FROM rust:latest
ENV SQLX_OFFLINE=true

WORKDIR /api-backend
COPY /backend .

RUN cargo build --release
EXPOSE 8000
CMD cargo run
