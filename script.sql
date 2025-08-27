CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS usuarios ( id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), 
									 nombres VARCHAR(100) NOT NULL, 
									 apellidos VARCHAR(100) NOT NULL, 
									 fecha_nacimiento DATE, direccion TEXT, 
									 telefono VARCHAR(20), 
									 correo_electronico VARCHAR(150) UNIQUE NOT NULL, 
									 salario_base NUMERIC(10, 2), 
									 password VARCHAR(255) NOT NULL, 
									 rol VARCHAR(50) DEFAULT 'USER', 
									 estado BOOLEAN DEFAULT TRUE, 
									 fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP );
