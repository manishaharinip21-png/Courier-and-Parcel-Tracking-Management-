# Software Requirements Specification

## Purpose
A desktop system to manage courier customers, delivery personnel, parcel bookings, tracking status, and delivery attempts.

## Functional requirements
1. Add, view, search, edit and delete customer records.
2. Maintain delivery-personnel records.
3. Register parcels with sender, receiver, address, parcel type, weight, charge and dates.
4. Generate a unique tracking number when one is not supplied.
5. Update parcel delivery status.
6. Search a parcel using its tracking number.
7. Record successful or failed delivery attempts and remarks.
8. Display dashboard counts.
9. Analyze shipment status and booking trends with Python.

## Non-functional requirements
- Java Swing desktop interface.
- PostgreSQL relational database.
- JDBC prepared statements for data values.
- Primary keys, foreign keys, unique and check constraints.
- Clear validation and database error messages.

## Technology
Java, Swing, JDBC, PostgreSQL, Maven, Python, Pandas, NumPy, Matplotlib.

## Limitations
Shipment status is manually updated. Live GPS, third-party courier APIs, payments, and automated notifications are not included.
