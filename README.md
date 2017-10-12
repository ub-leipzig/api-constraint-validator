API-Constraint-Validator
=======

A tool that tests specification constraints by evaluating a graph deserialization of a remote JSON-LD API.

The evaluation uses SPARQL and stream filtering.

IIIF Manifest Constraints Test Example:

`$ gradle test -Dtest.resource=https://media.nga.gov/public/manifests/nga_highlights.json`

### Reports

`$ gradle report`

See `summary-reports` directory

### Execution Logs

See `build/logs`

### Java
This project requires JDK9