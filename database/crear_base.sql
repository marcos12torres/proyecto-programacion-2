-- ============================================================
-- Sistema de Clínica / Consultorio
-- Script para preparar la base de datos MySQL.
--
-- Ejecutar UNA sola vez con un usuario administrador (root):
--     mysql -u root -p < database/crear_base.sql
--
-- Las TABLAS no se crean acá: DataNucleus las genera solo al
-- iniciar la aplicación (datanucleus.schema.autoCreateAll=true).
-- ============================================================

CREATE DATABASE IF NOT EXISTS clinica
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Usuario genérico del proyecto (el mismo que usa persistence.xml).
CREATE USER IF NOT EXISTS 'programacion2'@'localhost' IDENTIFIED BY 'Programacion2';

GRANT ALL PRIVILEGES ON clinica.* TO 'programacion2'@'localhost';

FLUSH PRIVILEGES;
