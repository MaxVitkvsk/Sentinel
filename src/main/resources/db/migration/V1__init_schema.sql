CREATE TABLE coins (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE coin_mappings (
    id UUID PRIMARY KEY,
    coin_id UUID NOT NULL REFERENCES coins(id) ON DELETE CASCADE,
    provider_name VARCHAR(50) NOT NULL,
    external_id VARCHAR(50) NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS coins_price_history_id_seq INCREMENT BY 50;

CREATE TABLE coins_price_history (
    id BIGINT PRIMARY KEY DEFAULT nextval('coins_price_history_id_seq'),
    coin_id UUID NOT NULL REFERENCES coins(id),
    amount DECIMAL(38, 11),
    currency VARCHAR(10) NOT NULL,
    observed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_coins_price_history_coin_id_time
ON coins_price_history(coin_id, observed_at DESC);

CREATE UNIQUE INDEX idx_coin_mappings_provider_name_external_id
ON coin_mappings(provider_name, external_id);