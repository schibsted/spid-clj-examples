# Getting started with the SPiD Clojure SDK

The following is a minimal example of using the Clojure SDK. It fetches the
`/endpoints` endpoint, which returns a description of all available endpoints.

**NB!** To run the example, you need to know your client ID and API secret.

## Usage

```sh
lein run <client-id> <secret>
```

Replace pointy bracketed items with your credentials.

This will print the JSON-decoded response from the server, which shows all
available endpoints along with details on how to interact with them.
