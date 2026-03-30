-- KEYS[1]: 중복 유저 확인용 Set
-- KEYS[2]: 재고 카운트 Key
-- ARGV[1]: 유저 ID

-- 1. 중복 발급 확인
if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
    return -1 -- 이미 존재함
end

-- 2. 재고 확인
local stock = tonumber(redis.call('GET', KEYS[2]))
if stock == nil or stock <= 0 then
    return 0 -- 재고 없음
end

-- 3. 차감 및 등록
redis.call('DECR', KEYS[2])
redis.call('SADD', KEYS[1], ARGV[1])

return 1 -- 성공