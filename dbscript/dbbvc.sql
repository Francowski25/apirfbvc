create database dbbvc;
use dbbvc;

create table tuser (
  idUser char(36) primary key,
  image varchar(150),
  dni char(8) not null unique,
  firstName varchar(70) not null,
  surName varchar(70) not null,
  email varchar(100) not null unique,
  password varchar(200) not null,
  cellPhone char(9) null,
  role varchar(30) not null,/*Administrador, Vendedor, Quimico*/
  status varchar(20) not null,/*Activo, Inactivo*/
  createdAt datetime not null,
  updatedAt datetime not null
);

create table tcategory (
  idCategory char(36) primary key,
  image varchar(150),
  name varchar(100) not null,
  status varchar(20) not null,
  createdAt datetime not null,
  updatedAt datetime not null
);

create table tlaboratory(
  idLaboratory char(36) primary key,
  image varchar(150) null,
  name varchar(100) not null,
  status varchar(20) not null,
  createdAt datetime not null,
  updatedAt datetime not null
);

create table tsupplier (
  idSupplier char(36) primary key,
  name varchar(100) not null,
  ruc varchar(20) unique,
  phone varchar(20),
  address varchar(150),
  email varchar(100),
  status varchar(100) not null,/*Activo, Inactivo*/
  createdAt datetime not null,
  updatedAt datetime not null
);

create table tproduct (
  idProduct char(36) primary key,
  name varchar(50) not null,
  description varchar(255),
  barcode varchar(50) null,
  image varchar(150) null,
  priceSale decimal(10,2) not null,
  requiresPrescription boolean not null default false,
  stockMinimum int not null default 5,
  status varchar(100) not null,/*Activo, Inactivo*/
  idCategory char(36),
  idLaboratory char(36),
  createdAt datetime not null,
  updatedAt datetime not null,
  
  foreign key(idLaboratory) references tlaboratory(idLaboratory),
  foreign key (idCategory) references tcategory(idCategory)
);

create table tlot (
  idLot char(36) primary key,
  code varchar(30) not null unique,
  expirationDate date not null,
  purchasePrice decimal(10,2) not null,
  currentStock int not null default 0,
  idProduct char(36),
  idSupplier char(36),
  createdAt datetime not null,
  updatedAt datetime not null,
  
  foreign key (idProduct) references tproduct(idProduct),
  foreign key (idSupplier) references tsupplier(idSupplier)
);

create table tinventory(
  idProduct char(36) primary key,
  stock int not null default 0 check(stock >= 0),
  lastUpdated datetime not null,
  
  foreign key(idProduct)references tproduct(idProduct)
);

create table tinventorymovement(
  idMovement char(36) primary key,
  type varchar(20) not null, /*Entrada, Salida, Ajuste_Positivo, Ajuste_Negativo*/
  observation varchar(255),
  movementDate datetime not null,
  idUser char(36) not null,
  
  foreign key(idUser) references tuser(idUser)
);

create table tinventorymovementdetail(
  idDetail char(36) primary key,
  quantity int not null,
  unitCost decimal(10,2),
  idMovement char(36) not null,
  idProduct char(36) not null,
  idLot char(36),
  
  foreign key(idMovement) references tinventorymovement(idMovement),
  foreign key(idProduct) references tproduct(idProduct),
  foreign key(idLot) references tlot(idLot)
);

-- 1. tuser
INSERT INTO tuser VALUES
('70abf4a7-4dd2-4fd1-b634-65d1952aeb7a', 'avatar.png', '75576009', 'Franco', 'Taype Huamani', 'taypehuamanifranco@gmail.com', '$2a$12$HkHAQZZu4Qx.vUow.hK2tOU61GZLFc0pcZfKL6QJcwUqLJIrP8Zfa', '918355614', 'administrador', 'activo', NOW(), NOW()),
('939dbc34-a1a7-4931-85a0-d291013c6973', 'avatar.png', '12837423', 'Ana', 'Torres', 'ana.torres@bvc.com', '$2a$12$bBICQYVrGmBrrhhhgbnhD.ozy1ynWKJUy9EB0NmbORWtXhCj7NEWO', '755734344', 'quimico', 'activo', NOW(), NOW()),
('5d828099-5b65-4516-99e2-6fc49ab51667', 'avatar.png', '34273434', 'Luis', 'Quispe', 'luis.quispe@bvc.com', '$2a$12$N.ZlEV3jMyMa.y3jRPFnieTigqX5nNIgf78xt4ZTdgwmlYgFf9qOq', '755246009', 'vendedor', 'activo', NOW(), NOW()),
('b7d85ece-611f-40be-94c5-1fb709a071af', 'avatar-f.png', '75524009', 'María', 'Huanca', 'maria.huanca@bvc.com', '$2a$12$KMHwk8UQSNphyE.aZWCNGujfAkRm8b21nenvmylQi5uiiP9NMNYKC', '123476009', 'vendedor', 'activo', NOW(), NOW()),
('403b00d6-3029-4c33-a732-47f746c98b45', 'avatar2.png', '98236743', 'Jorge', 'Condori', 'jorge.condori@bvc.com', '$2a$12$Usm4uFsKxHugZZS86C2iJeAYEx08.izM.0EsAAE.fAOCsmR9xXbu.', '928316009', 'quimico', 'inactivo', NOW(), NOW()),
('59c5d012-7bb4-4c81-80bb-64fbdcf18db0', 'avatar-f.png', '45892311', 'Claudia', 'Mendoza Ramos', 'claudia.mendoza@bvc.com', '$2a$12$Z7xK9pY5M9Jb7vR2wQ1eOu8vK3fG4h5j6k7l8m9n0o1p2q3r4s5t6', '954123789', 'quimico', 'activo', NOW(), NOW()),
('a12b34cd-56ef-78gh-90ij-1234567890ab', 'avatar.png', '72145698', 'Carlos', 'Espinoza Vega', 'carlos.espinoza@bvc.com', '$2a$12$Y3xO8pX4L8Ja6uQ1vP0dNt7uJ2eF3g4i5j6k7l8m9n0o1p2q3r4s5', '987654321', 'vendedor', 'activo', NOW(), NOW()),
('e847c211-c918-4b72-be77-d4a9f3ebac51', 'avatar-f.png', '09432175', 'Elena', 'Flores Choque', 'elena.flores@bvc.com', '$2a$12$X2wN7oW3K7Iz5tP0uO9cMs6tI1dE2f3h4i5j6k7l8m9n0o1p2q3r4', '961524376', 'vendedor', 'inactivo', NOW(), NOW()),
('3d9e8c7b-6a5f-4d3c-2b1a-0e9d8c7b6a5f', 'avatar.png', '25841397', 'Ricardo', 'Palomino Ortiz', 'ricardo.palomino@bvc.com', '$2a$12$W1vM6nW2J6Hy4sO9tN8bLr5sH0cD1e2g3h4i5j6k7l8m9n0o1p2q3', '932145678', 'quimico', 'activo', NOW(), NOW()),
('fa93b216-cdd2-4b21-8154-1b709a071afc', 'avatar-f.png', '47586912', 'Gabriela', 'Mamani Luna', 'gabriela.mamani@bvc.com', '$2a$12$V0uL5mV1I5Gx3rN8sM7aKq4rH9bC0d1f2g3h4i5j6k7l8m9n0o1p2', '993847561', 'quimico', 'activo', NOW(), NOW());

-- 2. tcategory
INSERT INTO tcategory VALUES
('5ad2ab8f-c672-48d1-a579-31e0b6cd8cb7', NULL, 'Analgésicos', 'activo', NOW(), NOW()),
('12268e2d-a223-4754-9264-c5781a547d08', NULL, 'Antibióticos', 'activo', NOW(), NOW()),
('91051daf-e3bc-43e3-a6c7-bde48ede5553', NULL, 'Vitaminas y Suplementos', 'activo', NOW(), NOW()),
('dc4bba06-75a7-4a25-b33e-f6c30c49b8f4', NULL, 'Antiinflamatorios', 'activo', NOW(), NOW()),
('2cfba2df-86ac-43bf-8f08-1b25d439fc33', NULL, 'Dermatológicos', 'activo', NOW(), NOW());

-- 3. tsupplier
INSERT INTO tsupplier VALUES
('8919bd47-ec65-4977-bd88-8abf1e3fb592', 'Farmacéutica Andina SAC', '20501234567', '084-223344', 'Av. La Cultura 123, Cusco', 'ventas@farmandina.com', 'activo', NOW(), NOW()),
('d788888b-01f6-4568-83e9-00552e04a83e', 'Distribuidora Medifarma', '20509876543', '01-4456789', 'Jr. Camaná 456, Lima', 'contacto@medifarma.com', 'activo', NOW(), NOW()),
('a8796fe8-9f15-4afc-9052-465bd6aca1f2', 'Química Suiza SRL', '20512345678', '01-6123456', 'Av. Argentina 789, Lima', 'pedidos@quimicasuiza.com', 'activo', NOW(), NOW()),
('06f473ed-1e42-4bbe-829c-0167996a657c', 'Laboratorios Induquímica', '20498765432', '054-334455', 'Calle Mercaderes 321, Arequipa', 'ventas@induquimica.com', 'activo', NOW(), NOW()),
('7c8b7a54-ae82-4e9b-be2b-df8df5899efa', 'Farmindustria Perú', '20534567890', '01-7891234', 'Av. Colonial 654, Lima', 'info@farmindustria.com', 'inactivo', NOW(), NOW());

-- 4. tlaboratory
INSERT INTO tlaboratory VALUES
('b1c23d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e', NULL, 'Laboratorio Portugal', 'activo', NOW(), NOW()),
('c2d34e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7f', NULL, 'Bayer Perú', 'activo', NOW(), NOW()),
('d34e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8a', NULL, 'Medifarma S.A.', 'activo', NOW(), NOW()),
('e45f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9b', NULL, 'Sanofi', 'activo', NOW(), NOW()),
('f56a7b8c-9d0e-1f2a-3b4c-5d6e7f8a9b0c', NULL, 'Hersil', 'activo', NOW(), NOW());

-- 5. tproduct
INSERT INTO tproduct VALUES
('57c10dc3-3e41-4bcc-906a-dbae1ca43e46', 'Paracetamol 500mg', 'Tabletas analgésicas y antipiréticas', '7501234567891', 'paracetamol.png', 0.50, false, 10, 'activo', '5ad2ab8f-c672-48d1-a579-31e0b6cd8cb7', 'b1c23d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e', '2026-06-09 08:15:00', '2026-06-09 08:15:00'),
('89ae8ff2-6971-4196-b508-dcf5141820fd', 'Amoxicilina 500mg', 'Cápsulas antibióticas de amplio espectro', '7509876543211', null, 1.20, true, 8, 'activo', '12268e2d-a223-4754-9264-c5781a547d08', 'c2d34e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7f', '2026-06-10 09:30:00', '2026-06-10 09:30:00'),
('7bec925e-c6ff-4059-a18b-a8b88738e24d', 'Vitamina C 1000mg', 'Tabletas efervescentes de vitamina C', '7512345678901', null, 0.80, false, 15, 'inactivo', '91051daf-e3bc-43e3-a6c7-bde48ede5553', 'd34e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8a', '2026-06-10 14:45:00', '2026-06-10 14:45:00'),
('28fdef5c-734d-4b17-949e-991e09488c7a', 'Ibuprofeno 400mg', 'Tabletas antiinflamatorias y analgésicas', '7534567890121', null, 0.70, false, 10, 'activo', 'dc4bba06-75a7-4a25-b33e-f6c30c49b8f4', 'e45f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9b', '2026-06-10 14:45:00', '2026-06-10 14:45:00'),
('1069e606-393a-4b90-acad-f20565152890', 'Clotrimazol Crema', 'Crema antifúngica para uso tópico', '7598765432101', null, 8.50, false, 5, 'activo', '2cfba2df-86ac-43bf-8f08-1b25d439fc33', 'f56a7b8c-9d0e-1f2a-3b4c-5d6e7f8a9b0c', '2026-06-15 17:30:00', '2026-06-15 17:30:00'),
('a1b2c3d4-e5f6-4789-9abc-def012345678', 'Loratadina 10mg', 'Tabletas antihistamínicas para alergias', '7601234567890', null, 0.90, false, 12, 'activo', '5ad2ab8f-c672-48d1-a579-31e0b6cd8cb7', 'b1c23d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e', '2026-06-12 09:00:00', '2026-06-12 09:00:00'),
('b2c3d4e5-f6a7-4890-abcd-ef0123456789', 'Omeprazol 20mg', 'Cápsulas para el tratamiento de acidez estomacal', '7602345678901', null, 1.50, false, 20, 'activo', '12268e2d-a223-4754-9264-c5781a547d08', 'c2d34e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7f', '2026-06-13 11:40:00', '2026-06-13 11:40:00'),
('c3d4e5f6-a7b8-4901-bcde-f0123456789a', 'Diclofenaco 50mg', 'Tabletas antiinflamatorias para dolor muscular y articular', '7603456789012', null, 0.75, false, 10, 'activo', 'dc4bba06-75a7-4a25-b33e-f6c30c49b8f4', 'd34e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8a', '2026-06-14 08:50:00', '2026-06-14 08:50:00'),
('d4e5f6a7-b8c9-4012-cdef-0123456789ab', 'Salbutamol Inhalador', 'Inhalador broncodilatador para el alivio del asma', '7604567890123', null, 18.90, true, 5, 'activo', '91051daf-e3bc-43e3-a6c7-bde48ede5553', 'e45f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9b', '2026-06-15 13:15:00', '2026-06-15 13:15:00'),
('e5f6a7b8-c9d0-4123-def0-123456789abc', 'Metformina 850mg', 'Tabletas para el control de la diabetes tipo 2', '7605678901234', null, 0.65, false, 30, 'activo', '2cfba2df-86ac-43bf-8f08-1b25d439fc33', 'f56a7b8c-9d0e-1f2a-3b4c-5d6e7f8a9b0c', '2026-06-15 13:15:00', '2026-06-15 13:15:00');

-- 6. tlot
INSERT INTO tlot VALUES
('a8e2cf6b-f651-4f98-a7af-d3fb12822f04', 'LOT-001', '2026-07-15', 0.25, 0,   '57c10dc3-3e41-4bcc-906a-dbae1ca43e46', '8919bd47-ec65-4977-bd88-8abf1e3fb592', NOW(), NOW()),
('1a4b31ec-104c-4467-936a-b4e4fd9f65f2', 'LOT-002', '2026-08-20', 0.60, 3,   '89ae8ff2-6971-4196-b508-dcf5141820fd', 'd788888b-01f6-4568-83e9-00552e04a83e', NOW(), NOW()),
('ce810b6e-9b7e-4c34-b376-4a66bad3e148', 'LOT-003', '2027-01-30', 0.40, 150, '7bec925e-c6ff-4059-a18b-a8b88738e24d', 'a8796fe8-9f15-4afc-9052-465bd6aca1f2', NOW(), NOW()),
('9972763f-9732-4660-a4cd-00eeabc2b6a3', 'LOT-004', '2026-09-15', 0.35, 10,  '28fdef5c-734d-4b17-949e-991e09488c7a', '06f473ed-1e42-4bbe-829c-0167996a657c', NOW(), NOW()),
('bb963302-3c56-4b2c-acf8-27e4dc7aa8bc', 'LOT-005', '2027-02-28', 4.20, 2,   '1069e606-393a-4b90-acad-f20565152890', '8919bd47-ec65-4977-bd88-8abf1e3fb592', NOW(), NOW()),
('f1a2b3c4-d5e6-4789-abcd-111111111111', 'LOT-006', '2027-03-15', 0.45, 0,   'a1b2c3d4-e5f6-4789-9abc-def012345678', 'd788888b-01f6-4568-83e9-00552e04a83e', NOW(), NOW()),
('f2a3b4c5-d6e7-4890-bcde-222222222222', 'LOT-007', '2027-04-20', 0.85, 180, 'b2c3d4e5-f6a7-4890-abcd-ef0123456789', 'a8796fe8-9f15-4afc-9052-465bd6aca1f2', NOW(), NOW()),
('f3a4b5c6-d7e8-4901-cdef-333333333333', 'LOT-008', '2026-12-10', 0.38, 8,   'c3d4e5f6-a7b8-4901-bcde-f0123456789a', '06f473ed-1e42-4bbe-829c-0167996a657c', NOW(), NOW()),
('f4a5b6c7-d8e9-4012-def0-444444444444', 'LOT-009', '2027-05-30', 12.50, 4,  'd4e5f6a7-b8c9-4012-cdef-0123456789ab', '8919bd47-ec65-4977-bd88-8abf1e3fb592', NOW(), NOW()),
('f5a6b7c8-d9e0-4123-ef01-555555555555', 'LOT-010', '2027-09-18', 0.30, 250, 'e5f6a7b8-c9d0-4123-def0-123456789abc', 'd788888b-01f6-4568-83e9-00552e04a83e', NOW(), NOW());
-- 7. tinventory
INSERT INTO tinventory VALUES
('57c10dc3-3e41-4bcc-906a-dbae1ca43e46', 0, NOW()),
('89ae8ff2-6971-4196-b508-dcf5141820fd', 3, NOW()),
('7bec925e-c6ff-4059-a18b-a8b88738e24d', 150, NOW()),
('28fdef5c-734d-4b17-949e-991e09488c7a', 10, NOW()),
('1069e606-393a-4b90-acad-f20565152890', 2, NOW()),
('a1b2c3d4-e5f6-4789-9abc-def012345678', 0, NOW()),
('b2c3d4e5-f6a7-4890-abcd-ef0123456789', 180, NOW()),
('c3d4e5f6-a7b8-4901-bcde-f0123456789a', 8, NOW()),
('d4e5f6a7-b8c9-4012-cdef-0123456789ab', 4, NOW()),
('e5f6a7b8-c9d0-4123-def0-123456789abc', 250, NOW());

-- 8. tinventorymovement
INSERT INTO tinventorymovement VALUES
('18f0e2bb-5e6c-446a-8f74-2e1d5d1efc97', 'Entrada', 'Compra inicial de Paracetamol', '2026-06-09 09:00:00', '70abf4a7-4dd2-4fd1-b634-65d1952aeb7a'),
('c5d58107-6aae-46e9-a1dc-2385d7af88c3', 'Entrada', 'Compra de Amoxicilina', '2026-06-10 10:15:00', '939dbc34-a1a7-4931-85a0-d291013c6973'),
('d32ad5b6-62cf-4542-a8f2-91b5c61760d5', 'Entrada', 'Ingreso de Vitamina C', '2026-06-10 15:00:00', '5d828099-5b65-4516-99e2-6fc49ab51667'),
('b7077b61-74c8-4fb2-a554-182f51dc7237', 'Salida', 'Venta mostrador', '2026-06-11 11:30:00', 'b7d85ece-611f-40be-94c5-1fb709a071af'),
('c7b8b993-6e04-4dba-ace8-d0e6a5dc41f9', 'Salida', 'Venta de Ibuprofeno', '2026-06-12 16:20:00', 'a12b34cd-56ef-78gh-90ij-1234567890ab'),
('df495a86-8a4e-4c69-ab8d-e733b399c676', 'Ajuste_Positivo', 'Corrección de inventario', '2026-06-13 09:45:00', '3d9e8c7b-6a5f-4d3c-2b1a-0e9d8c7b6a5f'),
('f5684cea-406b-4d8c-a450-2a5c40dc5ff5', 'Entrada', 'Ingreso de Omeprazol', '2026-06-14 08:30:00', 'fa93b216-cdd2-4b21-8154-1b709a071afc'),
('de5744a5-da10-42b0-be75-3e43a59b6856', 'Salida', 'Venta de Diclofenaco', '2026-06-15 13:40:00', '59c5d012-7bb4-4c81-80bb-64fbdcf18db0'),
('29417b69-8da1-4137-b816-4db6fbcee370', 'Entrada', 'Ingreso de Salbutamol', '2026-06-16 10:10:00', '70abf4a7-4dd2-4fd1-b634-65d1952aeb7a'),
('60691a38-58ba-441c-b668-540d4fc9f987', 'Ajuste_Negativo', 'Producto dañado', '2026-06-17 17:00:00', '939dbc34-a1a7-4931-85a0-d291013c6973');

-- 9. tinventorymovementdetail
INSERT INTO tinventorymovementdetail VALUES
('det-001-1111-1111-111111111111111111', 100, 0.25, '18f0e2bb-5e6c-446a-8f74-2e1d5d1efc97', '57c10dc3-3e41-4bcc-906a-dbae1ca43e46', 'a8e2cf6b-f651-4f98-a7af-d3fb12822f04'),
('det-002-2222-2222-222222222222222222', 50, 0.60, 'c5d58107-6aae-46e9-a1dc-2385d7af88c3', '89ae8ff2-6971-4196-b508-dcf5141820fd', '1a4b31ec-104c-4467-936a-b4e4fd9f65f2'),
('det-003-3333-3333-333333333333333333', 150, 0.40, 'd32ad5b6-62cf-4542-a8f2-91b5c61760d5', '7bec925e-c6ff-4059-a18b-a8b88738e24d', 'ce810b6e-9b7e-4c34-b376-4a66bad3e148'),
('det-004-4444-4444-444444444444444444', 10, 0.25, 'b7077b61-74c8-4fb2-a554-182f51dc7237', '57c10dc3-3e41-4bcc-906a-dbae1ca43e46', 'a8e2cf6b-f651-4f98-a7af-d3fb12822f04'),
('det-005-5555-5555-555555555555555555', 5, 0.35, 'c7b8b993-6e04-4dba-ace8-d0e6a5dc41f9', '28fdef5c-734d-4b17-949e-991e09488c7a', '9972763f-9732-4660-a4cd-00eeabc2b6a3'),
('det-006-6666-6666-666666666666666666', 3, 4.20, 'df495a86-8a4e-4c69-ab8d-e733b399c676', '1069e606-393a-4b90-acad-f20565152890', 'bb963302-3c56-4b2c-acf8-27e4dc7aa8bc'),
('det-007-7777-7777-777777777777777777', 180, 0.85, 'f5684cea-406b-4d8c-a450-2a5c40dc5ff5', 'b2c3d4e5-f6a7-4890-abcd-ef0123456789', 'f2a3b4c5-d6e7-4890-bcde-222222222222'),
('det-008-8888-8888-888888888888888888', 8, 0.38, 'de5744a5-da10-42b0-be75-3e43a59b6856', 'c3d4e5f6-a7b8-4901-bcde-f0123456789a', 'f3a4b5c6-d7e8-4901-cdef-333333333333'),
('det-009-9999-9999-999999999999999999', 4, 12.50, '29417b69-8da1-4137-b816-4db6fbcee370', 'd4e5f6a7-b8c9-4012-cdef-0123456789ab', 'f4a5b6c7-d8e9-4012-def0-444444444444'),
('det-010-aaaa-aaaa-aaaaaaaaaaaaaaaaaa', 2, 0.30, '60691a38-58ba-441c-b668-540d4fc9f987', 'e5f6a7b8-c9d0-4123-def0-123456789abc', 'f5a6b7c8-d9e0-4123-ef01-555555555555');

-- Comprobación final
select * from tproduct;
select * from tuser;
select * from tcategory;
select * from tlaboratory;
select * from tsupplier;
