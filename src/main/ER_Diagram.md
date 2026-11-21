```mermaid
erDiagram
    CUSTOMERS ||--o{ PARCELS : books
    DELIVERY_PERSONNEL ||--o{ PARCELS : handles
    PARCELS ||--o{ DELIVERY_ATTEMPTS : has
```
