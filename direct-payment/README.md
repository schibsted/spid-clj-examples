# spid-clojure-direct-payment-example

A bare-bones Clojure implementation of the SPiD Direct payment API, with
paylinks fallback for users without a valid credit card in their account.

## Usage

1. **Fill in the configuration**

   ```sh
   cd direct-payment/config
   cp config.edn.sample config.edn
   vi config.edn
   ```

   Replace `:client-id` and `:client-secret` with your own credentials.

2. **Start the server**

   ```sh
   lein ring server-headless
   ```

3. Visit [http://localhost:3015](http://localhost:3015) and try shopping. Use a
   [test credit card](http://techdocs.spid.no/test-credit-cards/) to pay in the
   staging environment.

To try both approaches, first delete any credit cards from your test account.
Then buy a movie, and observe how you are sent to SPiD to pay. This is the
paylink fallback. After completing checkout, try another purchase. This time,
you should not be redirected to SPiD at all.
