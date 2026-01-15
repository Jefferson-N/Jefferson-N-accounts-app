
CREATE DATABASE IF NOT EXISTS accounts_db;
USE accounts_db;

CREATE TABLE personas (
    id VARCHAR(36) PRIMARY KEY,
    tipo_persona VARCHAR(31) NOT NULL,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    age INT NOT NULL,
    identification VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(100),
    status BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_identification (identification),
    INDEX idx_tipo_persona (tipo_persona)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cuentas (
    id VARCHAR(36) PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    initial_balance DECIMAL(19, 2) NOT NULL,
    current_balance DECIMAL(19, 2) NOT NULL,
    status BOOLEAN DEFAULT TRUE,
    cliente_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES personas(id) ON DELETE CASCADE,
    INDEX idx_account_number (account_number),
    INDEX idx_cliente_id (cliente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE movimientos (
    id VARCHAR(36) PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
    cuenta_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cuenta_id) REFERENCES cuentas(id) ON DELETE CASCADE,
    INDEX idx_cuenta_id (cuenta_id),
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_personas_name ON personas(name);
CREATE INDEX idx_personas_status ON personas(status);
CREATE INDEX idx_cuentas_status ON cuentas(status);
CREATE INDEX idx_movimientos_cuenta_fecha ON movimientos(cuenta_id, fecha);

-- INSERT INTO personas (id, tipo_persona, name, gender, age, identification, address, phone, password, status)
-- VALUES
-- ('c1', 'CLIENTE', 'Jose Lema', 'Masculino', 30, '1234567890', 'Otavalo y su principal', '0985247885', '1234', TRUE),
-- ('c2', 'CLIENTE', 'Marianela Montalvo', 'Femenino', 28, '0987654321', 'Amazonas y NNUU', '0975498565', '5678', TRUE),
-- ('c3', 'CLIENTE', 'Juan Osorio', 'Masculino', 35, '1122334455', '13 junio y Equinoccial', '0987487587', '1245', TRUE);

-- -- Insertar cuentas para Jose Lema
-- INSERT INTO cuentas (id, account_number, account_type, initial_balance, current_balance, status, cliente_id)
-- VALUES
-- ('acc1', '478578', 'AHORRO', 2000.00, 2000.00, TRUE, 'c1'),
-- ('acc5', '585545', 'CORRIENTE', 1000.00, 1000.00, TRUE, 'c1');

-- -- Insertar cuentas para Marianela Montalvo
-- INSERT INTO cuentas (id, account_number, account_type, initial_balance, current_balance, status, cliente_id)
-- VALUES
-- ('acc2', '225487', 'CORRIENTE', 1000.00, 1000.00, TRUE, 'c2'),
-- ('acc4', '496825', 'AHORRO', 540.00, 540.00, TRUE, 'c2');

-- -- Insertar cuentas para Juan Osorio
-- INSERT INTO cuentas (id, account_number, account_type, initial_balance, current_balance, status, cliente_id)
-- VALUES
-- ('acc3', '452578', 'CORRIENTE', 2000.00, 2000.00, TRUE, 'c3');

-- -- Insertar movimientos de ejemplo
-- INSERT INTO movimientos (id, fecha, transaction_type, amount, balance, cuenta_id)
-- VALUES
-- ('mov1', '2022-02-08 10:00:00', 'DEBITO', 575.00, 1425.00, 'acc1'),
-- ('mov2', '2022-02-10 11:00:00', 'CREDITO', 600.00, 1600.00, 'acc2'),
-- ('mov3', '2022-02-12 12:00:00', 'CREDITO', 150.00, 2150.00, 'acc3'),
-- ('mov4', '2022-02-08 09:00:00', 'DEBITO', 540.00, 0.00, 'acc4');



