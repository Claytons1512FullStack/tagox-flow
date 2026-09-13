CREATE TABLE usuario_role (

                              usuario_id UUID NOT NULL,

                              role_id UUID NOT NULL,


                              CONSTRAINT pk_usuario_role
                                  PRIMARY KEY (usuario_id, role_id),


                              CONSTRAINT fk_usuario_role_usuario
                                  FOREIGN KEY (usuario_id)
                                      REFERENCES usuario(id)
                                      ON DELETE CASCADE,


                              CONSTRAINT fk_usuario_role_role
                                  FOREIGN KEY (role_id)
                                      REFERENCES role(id)
                                      ON DELETE RESTRICT
);