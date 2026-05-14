import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const LOCK_TYPE = __ENV.LOCK_TYPE || 'named';
const VUS = Number(__ENV.VUS || 30);
const DURATION = __ENV.DURATION || '30s';
const USER_COUNT = Number(__ENV.USER_COUNT || 50);
const SLEEP_SECONDS = Number(__ENV.SLEEP_SECONDS || 0);

const expectedBusinessResult = new Rate('expected_business_result');
const lockFailure = new Rate('lock_failure');
const measuredRequests = new Counter('measured_reqs');
const measuredRequestDuration = new Trend('measured_req_duration', true);

const endpoints = {
  named: 'add-new-ticket-final',
  redisson: 'add-new-ticket-with-redisson',
};

export const options = {
  scenarios: {
    lock_comparison: {
      executor: 'constant-vus',
      vus: VUS,
      duration: DURATION,
      gracefulStop: '0s',
      tags: {
        lock_type: LOCK_TYPE,
      },
    },
  },
};

export function setup() {
  const endpoint = resolveEndpoint();
  http.post(`${BASE_URL}/users/1/${endpoint}`, null, {
    tags: {
      lock_type: LOCK_TYPE,
      phase: 'warmup',
    },
    responseCallback: null,
  });
}

export default function () {
  const endpoint = resolveEndpoint();

  const userId = resolveUserId();
  const response = http.post(`${BASE_URL}/users/${userId}/${endpoint}`, null, {
    tags: {
      lock_type: LOCK_TYPE,
      phase: 'measure',
      endpoint,
    },
  });

  const body = response.body || '';
  const isBusinessResult = response.status === 200;
  const isLockFailure = body.includes('LOCK 을 수행하는 중에 오류가 발생하였습니다');

  expectedBusinessResult.add(isBusinessResult);
  lockFailure.add(isLockFailure);
  measuredRequests.add(1);
  measuredRequestDuration.add(response.timings.duration);

  check(response, {
    'business result returned': () => isBusinessResult,
    'lock did not fail': () => !isLockFailure,
  });

  if (SLEEP_SECONDS > 0) {
    sleep(SLEEP_SECONDS);
  }
}

function resolveUserId() {
  return ((__VU + __ITER) % USER_COUNT) + 1;
}

function resolveEndpoint() {
  const endpoint = endpoints[LOCK_TYPE];
  if (!endpoint) {
    throw new Error(`Unsupported LOCK_TYPE=${LOCK_TYPE}. Use one of: named, redisson`);
  }
  return endpoint;
}
