FROM messense/rust-musl-cross:x86_64-musl as builder
ENV SQLX_OFFLINE=true
WORKDIR /air-backend

COPY /backend .
RUN cargo build --release --target x86_64-unknown-linux-musl
RUN ls

# Create a new stage with a minimal image
FROM scratch
COPY --from=builder /air-backend/target/x86_64-unknown-linux-musl/release/backend /air-backend
ENTRYPOINT ["/air-backend"]
EXPOSE 8000