import os
import pandas as pd
import matplotlib.pyplot as plt
from sqlalchemy import create_engine

url = os.getenv("COURIER_DB_URL", "postgresql+psycopg2://postgres:CHANGE_ME@localhost:5432/courier_management")
engine = create_engine(url)
df = pd.read_sql("SELECT tracking_number, booking_date, status, weight_kg, delivery_charge FROM parcels", engine)

if df.empty:
    print("No parcel records yet. Add parcels in the Java application.")
    raise SystemExit(0)

print("PARCEL STATUS SUMMARY")
print(df["status"].value_counts())
print("\nNUMERIC SUMMARY")
print(df[["weight_kg", "delivery_charge"]].describe().round(2))

df["status"].value_counts().plot(kind="bar", title="Parcels by Status", xlabel="Status", ylabel="Count")
plt.tight_layout()
plt.savefig("analytics/parcel_status.png")
plt.show()

df["booking_date"] = pd.to_datetime(df["booking_date"])
df.groupby(df["booking_date"].dt.date).size().plot(kind="line", marker="o", title="Daily Parcel Bookings", xlabel="Date", ylabel="Bookings")
plt.tight_layout()
plt.savefig("analytics/daily_bookings.png")
plt.show()
