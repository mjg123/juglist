# juglist

Some code to produce the Java User Group directory website

## How to?

  1. Clone this repo
  1. Set your `TWITTER_BEARER_TOKEN` env var (how to get?)
  1. Run `lein run fetch-data`
  1. Run `lein cljsbuild once min`
  
### What does this do?

  - Using your bearer token to authenticate to the Twitter API, the JSON representation of the big Twitter list 'o JUGs is downloaded.
  - Each of those JUGs has its `location` field geocoded. There is a list of exceptions in the code for JUG accounts which are known to have a non-geocodable `location`. Geocoding is performed by [OSM Nominatim](https://nominatim.openstreetmap.org/). Any JUGs which cannot be geolocated and have not been special cased will be logged to stderr with a warning.
  - In both steps above the results are cached into a directory called `cache` which will be created in the project root
  - The output of this stage is some clojurescript source representing all the JUGs in `src/cljs`. There is other code in there which will use it, and we can compile the whole shebang with `lein cljsbuild once min`
  - This puts a `juglist.js` file into the `web` folder, where there is already a `map.js`. Load these up in your browser, or move them over to your static host.
