-- Run CREATE DATABASE separately, then connect to courier_management and run remaining SQL.
CREATE DATABASE courier_management;
-- Customers
CREATE TABLE customers(customer_id SERIAL PRIMARY KEY, full_name VARCHAR(100) NOT NULL, phone VARCHAR(20) NOT NULL, email VARCHAR(120), address TEXT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE delivery_personnel(personnel_id SERIAL PRIMARY KEY, full_name VARCHAR(100) NOT NULL, phone VARCHAR(20) NOT NULL, vehicle_number VARCHAR(30), active BOOLEAN NOT NULL DEFAULT TRUE);
CREATE TABLE parcels(parcel_id SERIAL PRIMARY KEY, tracking_number VARCHAR(30) UNIQUE NOT NULL, sender_id INTEGER NOT NULL REFERENCES customers(customer_id), receiver_name VARCHAR(100) NOT NULL, receiver_phone VARCHAR(20) NOT NULL, delivery_address TEXT NOT NULL, parcel_type VARCHAR(60) NOT NULL, weight_kg NUMERIC(8,2) NOT NULL CHECK(weight_kg>0), delivery_charge NUMERIC(10,2) NOT NULL CHECK(delivery_charge>=0), booking_date DATE NOT NULL DEFAULT CURRENT_DATE, expected_delivery DATE, status VARCHAR(25) NOT NULL DEFAULT 'Booked' CHECK(status IN ('Booked','Picked Up','In Transit','Out for Delivery','Delivered','Delivery Failed','Returned')), personnel_id INTEGER REFERENCES delivery_personnel(personnel_id), notes TEXT);
CREATE TABLE delivery_attempts(attempt_id SERIAL PRIMARY KEY, parcel_id INTEGER NOT NULL REFERENCES parcels(parcel_id) ON DELETE CASCADE, attempt_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, attempt_status VARCHAR(30) NOT NULL CHECK(attempt_status IN ('Successful','Failed')), remarks TEXT);
CREATE INDEX idx_parcels_tracking ON parcels(tracking_number);
CREATE INDEX idx_parcels_status ON parcels(status);
