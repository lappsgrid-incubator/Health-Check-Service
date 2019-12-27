# LAPPS Grid Health Check Service

The LAPPS Grid Health Check Service monitors the online status of the LAPPS Grid services and notifies administrators if any of the services go offline.

A list of registered services in obtained from each Service Manager instance once every 24 hours, and each service will have its `getMetadata` method called once per hour.

The Health Check Service also exposes a [/ping](https://api.lappsgrid.org/health/ping) endpoint to detect if the health check service is online.  The script in `src/main/scripts/cron.hourly` calls the `/ping` endpoint and sends and email notification if the Health Check service does not respong.  The cron script should not be run on the same server the Health Check service is running on.

Currently the Health Check service runs on the LAPPS Grid Docker swarm on Jetstream and the cron script runs on Vassar's ANC server.


