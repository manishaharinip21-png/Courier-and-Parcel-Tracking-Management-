# Courier and Parcel Tracking Management System

Java Swing + PostgreSQL + Python analytics student project.

## Features
- Customer and delivery-personnel management
- Parcel booking with generated tracking number
- Add/view/search/edit/delete records
- Delivery status management
- Tracking lookup with delivery-attempt history
- Dashboard counts
- Python status and booking-trend charts

## Setup
1. Install JDK 17+, Maven, PostgreSQL, and Python 3.10+.
2. In pgAdmin, create database `courier_management`. Run `database/schema.sql` in two parts: first `CREATE DATABASE` separately; then connect to the new database and run the table/index statements.
3. Edit `src/main/resources/db.properties`; set your PostgreSQL password. Do not share this password.
4. Open this folder in VS Code.
5. In terminal run `mvn clean package`.
6. Run `java -jar target/courier-parcel-management-1.0.0-jar-with-dependencies.jar`.

The first Maven build needs internet to download dependencies.

## Test flow
Add a customer; note customer ID. Add delivery personnel; note personnel ID. Add a parcel with sender ID, receiver details, weight, charge, dates and status. Leave tracking number blank to generate one. Use the Track Parcel tab to search its tracking number. Update parcel status and add delivery attempt records.

Dates: YYYY-MM-DD. Delivery-attempt timestamp: YYYY-MM-DD HH:MM:SS. Boolean: true/false.

## Python analytics
Run `python -m pip install -r analytics/requirements.txt`. Update the database URL in `analytics/parcel_analytics.py` with your local password, then run `python analytics/parcel_analytics.py`.

## Scope note
Tracking displays information manually entered into this database. This version does not include real courier-company integration, live GPS, SMS/email notifications, or online payments.
