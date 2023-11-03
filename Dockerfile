FROM messense/rust-musl-cross:x86_64-musl as builder
ENV SQLX_OFFLINE=true

WORKDIR /api-backend
COPY /backend .
# Generate info for caching dependencies
RUN cargo build --release --target x86_64-unknown-linux-musl

FROM scratch
COPY --from=builder /api-backend/target/x86_64-unknown-linux-musl/release/api-backend /api-backend
ENTRYPOINT ["/api-backend"]
EXPOSE 8000
