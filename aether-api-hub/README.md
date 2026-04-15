# AetherAPI-Hub

AetherAPI-Hub is a customized Java DDD multi-module skeleton.

## Structure

```text
aether-api-hub/
|- aether-api-hub-app
\- aether-api-hub-standard
   |- aether-api-hub-api
   |- aether-api-hub-adapter
   |- aether-api-hub-client
   |- aether-api-hub-domain
   |- aether-api-hub-infrastructure
   \- aether-api-hub-service
```

## Notes

- The original template module names were renamed to the `aether-api-hub-*` convention.
- The original `docs` folder was removed.
- Maven coordinates now use the `io.github.aetherapihub` group and `aether-api-hub-*` artifact names.
