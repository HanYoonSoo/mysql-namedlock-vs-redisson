# MySQL Named Lock vs Redisson

이 프로젝트는 MySQL의 Named Lock을 분산 락처럼 사용할 때 주의해야 할 커넥션 풀 문제를 확인하고, 이를 멀티 커넥션 풀 구조로 해결한 뒤 Redis 기반 Redisson Lock과 성능을 비교하기 위한 실험 프로젝트입니다.

핵심 관심사는 단순히 “Named Lock을 사용할 수 있는가”가 아니라, 애플리케이션의 기존 DB 커넥션 풀을 그대로 Named Lock에 사용했을 때 발생할 수 있는 문제를 어떻게 피할 것인가입니다.

## 프로젝트 목적

MySQL Named Lock은 `GET_LOCK`, `RELEASE_LOCK` 함수를 통해 특정 이름의 락을 획득하고 해제합니다. 이 락은 트랜잭션이 아니라 MySQL 커넥션에 귀속됩니다.

따라서 Spring 애플리케이션에서 기존 비즈니스 로직용 커넥션 풀을 그대로 Named Lock 획득에도 사용하면 다음과 같은 문제가 생길 수 있습니다.

- 락을 잡은 커넥션과 비즈니스 로직에서 사용하는 커넥션의 생명주기를 명확히 분리하기 어렵다.
- Named Lock 대기 요청이 기존 DB 커넥션 풀을 점유해 일반 쿼리 처리까지 지연시킬 수 있다.
- 동시 요청이 많아질수록 락 대기와 비즈니스 쿼리가 같은 풀을 경쟁하게 된다.
- 커넥션 풀 고갈이 발생하면 락 처리 문제인지 일반 DB 처리 문제인지 구분하기 어려워진다.

이 프로젝트는 위 문제를 피하기 위해 Named Lock 전용 DataSource와 비즈니스 로직용 DataSource를 분리합니다.

즉, MySQL Named Lock을 사용할 때 다음 구조를 비교합니다.

- 기본 커넥션 풀만 사용하는 방식
- Named Lock 전용 커넥션 풀을 별도로 두는 방식
- Redis 기반 Redisson Lock을 사용하는 방식

## 핵심 아이디어

Named Lock은 커넥션 단위로 유지되기 때문에 락 획득부터 해제까지 같은 커넥션을 사용해야 합니다.

이 프로젝트의 최종 Named Lock 구현은 다음 흐름을 따릅니다.

1. Named Lock 전용 DataSource에서 커넥션을 하나 가져온다.
2. 해당 커넥션으로 `GET_LOCK(lockName, timeout)`을 실행한다.
3. 락을 획득한 상태에서 실제 비즈니스 로직을 실행한다.
4. 같은 커넥션으로 `RELEASE_LOCK(lockName)`을 실행한다.
5. 락 전용 커넥션을 반환한다.

비즈니스 로직에서 사용하는 JPA/Hikari 커넥션 풀과 락 획득에 사용하는 커넥션 풀을 분리함으로써, 락 대기 요청이 일반 DB 작업용 커넥션을 모두 점유하는 상황을 줄일 수 있습니다.

## 비교 대상

### MySQL Named Lock - 기본 커넥션 풀 사용

애플리케이션의 기본 DataSource를 이용해 Named Lock을 획득합니다.

이 방식은 구현이 단순하지만 락 대기와 일반 DB 작업이 같은 커넥션 풀을 사용합니다. 동시 요청이 많아지면 락을 기다리는 요청이 커넥션을 점유하고, 그 결과 실제 비즈니스 쿼리가 사용할 커넥션까지 부족해질 수 있습니다.

### MySQL Named Lock - 멀티 커넥션 풀 사용

기본 DataSource와 별도로 `userlock.datasource`를 두고 Named Lock 전용 커넥션 풀을 사용합니다.

락 처리는 락 전용 풀에서만 수행하고, 비즈니스 로직은 기존 JPA용 풀을 사용합니다. 이 방식은 MySQL Named Lock을 분산 락처럼 사용할 때 이 프로젝트가 제안하는 주요 구조입니다.

관련 구현:

- [UserLevelLockFinal](src/main/java/com/lock/mysql_namedlock_vs_redisson/lock/UserLevelLockFinal.java)
- [MysqlNamedlockVsRedissonApplication](src/main/java/com/lock/mysql_namedlock_vs_redisson/MysqlNamedlockVsRedissonApplication.java)
- [application.yml](src/main/resources/application.yml)

### Redis Redisson Lock

Redis를 별도 락 저장소로 사용하고 Redisson의 `RLock`을 통해 분산 락을 획득합니다.

MySQL Named Lock 방식과 비교해 락을 위한 저장소가 DB와 분리되어 있고, 분산 락 라이브러리에서 제공하는 API를 사용할 수 있다는 장점이 있습니다. 이 프로젝트에서는 같은 비즈니스 로직을 대상으로 Redisson Lock과 멀티 커넥션 풀 기반 Named Lock의 성능 차이를 비교합니다.

관련 구현:

- [UserLevelLockWithRedisson](src/main/java/com/lock/mysql_namedlock_vs_redisson/lock/UserLevelLockWithRedisson.java)
- [RedisConfig](src/main/java/com/lock/mysql_namedlock_vs_redisson/config/RedisConfig.java)

## 기술 스택

- Java 17
- Spring Boot 3.5.14
- Spring Data JPA
- MySQL
- Flyway
- Redis
- Redisson
- k6

## 실행 준비

기본 설정은 [application.yml](src/main/resources/application.yml)에 정의되어 있습니다.

MySQL:

- host: `localhost`
- database: `user_lock`
- username: `root`
- password: `root`

Redis:

- host: `localhost`
- port: `15501`
- password: `password`

MySQL 데이터베이스 생성:

```bash
mysql -uroot -proot -e "create database if not exists user_lock;"
```

Docker로 MySQL을 실행하는 예시:

```bash
docker run --name user-lock-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=user_lock \
  -p 3306:3306 \
  -d mysql:8
```

Docker로 Redis를 실행하는 예시:

```bash
docker run --name user-lock-redis \
  -p 15501:6379 \
  -d redis:7 redis-server --requirepass password
```

## 애플리케이션 실행

```bash
./gradlew bootRun
```

애플리케이션 시작 시 Flyway가 초기 스키마와 테스트 사용자를 생성합니다.

주의: 현재 프로젝트의 `FlywayMigrationStrategy`는 시작 시 `clean -> baseline -> migrate`를 수행합니다. 따라서 애플리케이션을 다시 실행하면 기존 테이블과 데이터가 초기화됩니다.

## 성능 비교 실행

k6 스크립트는 [scripts/k6/lock-comparison.js](scripts/k6/lock-comparison.js)에 있습니다.

MySQL Named Lock, 멀티 커넥션 풀 방식:

```bash
k6 run scripts/k6/lock-comparison.js \
  -e LOCK_TYPE=named \
  -e VUS=30 \
  -e DURATION=30s \
  -e USER_COUNT=50
```

Redisson Lock 방식:

```bash
k6 run scripts/k6/lock-comparison.js \
  -e LOCK_TYPE=redisson \
  -e VUS=30 \
  -e DURATION=30s \
  -e USER_COUNT=50
```

주요 환경 변수:

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `BASE_URL` | `http://localhost:8080` | 테스트 대상 서버 주소 |
| `LOCK_TYPE` | `named` | `named` 또는 `redisson` |
| `VUS` | `30` | 동시 가상 사용자 수 |
| `DURATION` | `30s` | 테스트 지속 시간 |
| `USER_COUNT` | `50` | 요청을 분산할 사용자 수 |
| `SLEEP_SECONDS` | `0` | 각 반복 사이 대기 시간 |

## 테스트

```bash
./gradlew test
```
