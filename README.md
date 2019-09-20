# blogify

All-in-one blogging engine powered by Ktor and Angular.

## Architecture

Blogify uses `Ktor` on the backend and `Angular` on the frontend.

It uses `PostgreSQL` as a database.

## Performance

Due to the current age of the project, performance and optimization are not a significant focus.

Currently, our own (very basic) testing shows that the server can handle upwards of 3,000 requests/sec. after warm-up on a listing endpoint.

## Building and deploying

The default deploy configuration runs the backend and the database as `docker-compose` services. A functioning, local, test deployment can be achieved by running the `localTestDeploy` gradle task.

## Core team

### Backend
- Benjamin Dupont
- Muhammad Hamza

### Frontend
- Lucy Agamaite
- Muhammad Hamza

### Database
- Stan Lyakhov

### UI Design
- Seamus Donnellan

## Disclaimer

This project is in a very early, pre-alpha stage.

Any versions indicated in the startup message starting with `PRX` are not safe to use, not tested and are not guaranteed to retain data safely.

## Contributing

We accept pull requests :)
