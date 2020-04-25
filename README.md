# RCSB-MOJAVE-TOOLS
This project contains tools to work with JSON schemas. You can find tools for:

#### Schema loading
You can use `org.rcsb.mojave.tools.jsonschema.SchemaLoader` API to read and write schemas.

#### Schema filtering 
Currently filtering doesn’t support whitelisting at the level of children nodes. To specify root nodes to be included, 
use `org.rcsb.mojave.tools.jsonschema.traversal.VisitableSchemaTree.withPropertiesFilter()` configuration option.
 
#### Schema stitching
Use `org.rcsb.mojave.tools.jsonschema.SchemaStitching.mergeSchemas(JsonNode targetSchema, JsonNode updateSchema)` API 
to stitch two schemas together. Here is a set of rules that govern schema stitching:
 - Non-overlapping elements that only appear in one source will be copied to the resulting schema.
 - Overlapping elements that describe different content: the content of both elements will be copied to the 
resulting schema (an overlapping element scope will be expanded).
 - Overlapping elements that describe same content: the content from `updateSchema` will supersede an overlapping content
from `targetSchema` (an overlapping element scope will be overwritten).

##### Schema integration rules
As we integrate multiple sources into a single schema, we want to ensure that the original schema is preserved 
as closely as possible. However, it may be necessary to change the original schema. When this happens a new or modified 
data item is appended to the original schema under `rcsb_` namespace to indicate provenance. Here is a set of rules that 
govern schema integration:
  - Data mutation (e.g. changing or adding new value) should be added as a new `rcsb_` item.
  - Data aggregation (e.g. merging multiple original data items in a new object) should be added as a new `rcsb_` item.
  - Data reduction (e.g. filtering of array) should be added as a new `rcsb_` item.
  - Schema reduction (e.g. removing fields from original data item) should be integrated with original schema.

