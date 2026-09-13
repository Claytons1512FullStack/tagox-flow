CREATE TABLE role (

                      id UUID PRIMARY KEY,

                      tipo VARCHAR(50) NOT NULL UNIQUE,

                      descricao VARCHAR(150) NOT NULL

);