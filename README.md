# RCSB-MOJAVE-TOOLS
This repository contains tools to work with JSON schemas. You can use it for:

#### Loading JSON Schemas
You can use `org.rcsb.mojave.tools.jsonschema.SchemaLoader` API to read and write schemas.

#### Resolving JSON References ($ref)
`org.rcsb.mojave.tools.jsonschema.SchemaRefResolver` aims at resolving all JSON References until a final document 
is reached. The only supported mode is "inline" resolution. This means input schema will be modified inplace and resolved 
`$ref` fragments will be incorporated into the original schema.

#### Stitching JSON Schemas
Use `org.rcsb.mojave.tools.jsonschema.SchemaStitching.mergeSchemas(JsonNode targetSchema, JsonNode updateSchema)` 
to stitch two schemas together. Here is a set of rules that govern schema stitching:
 - Non-overlapping elements that only appear in one source will be copied to the resulting schema.
 - Overlapping elements that describe different content: the content of both elements will be copied to the 
resulting schema (an overlapping element scope will be expanded).
 - Overlapping elements that describe same content: the content from `updateSchema` will supersede an overlapping content
from `targetSchema` (an overlapping element scope will be overwritten).

#### Generating Java Types from JSON Schemas

##### POJOs generation and documentation
 [**jsonschema2pojo**](https://github.com/joelittlejohn/jsonschema2pojo) Maven plugin is used to generate POJOs. When 
 [**jsonschema2pojo-maven-plugin:generate**](https://joelittlejohn.github.io/jsonschema2pojo/site/0.5.1/generate-mojo.html) 
is invoked during the build process, this goal generates Java types and can annotate those types for data-binding and 
validation. To add more _Mojave_ models:
- (i) add new JSON schema file under `src/main/resources/schema` if this schema should be manually curated; 
or modify and existing JSON schema for available cores;
- (ii) if a new schema should be stitched, add an argument to the configuration of an appropriate execution of 
`org.rcsb.mojave.tools.core.GenerateJsonSchemaCores` in the project pom.xml that points to the location of a schema
to be added. 

Based on this configuration Java sources will be automatically generated and placed into project build directory 
(`target/generated-sources/classes`) before compile phase.

Automatically generated POJOs are used in _Yosemite_ to build GraphQL API and its documentation.

##### Configuration options
**jsonschema2pojo-maven-plugin** offers flexible configuration through specifying optional parameters. 
Please, consult with official documentation for more details: 
https://joelittlejohn.github.io/jsonschema2pojo/site/0.5.1/generate-mojo.html.

##### Java type extension
In order to reuse Java types definitions in GraphQL schema it is required that all types 
have unique names. At the moment all GraphQL types share one global namespace and if field name is unambiguous 
withing the resulting type it results in incorrect wiring of GraphQL types in the final GraphQL schema. 
The name conflicts can be resolved by adding with an extension property `javaType` supported by jsonschema2pojo tool.
The keyword appears in the schema definition and allows specifying a fully qualified name for the generated Java type. 

##### Customizing generated classes
There are cases in which you might want to modify the default behavior of jsonschema2pojo tool. Some of these include:
- modifying existing/adding custom annotations for generated types
- modifying or extending supported JSON schema rules that can customize generated types

You can implement custom annotator and custom rules factory. To use them with the Maven plugin, 
add the necessary artifacts as a dependency of the plugin and set the `customAnnotator` and/or `customRuleFactory` to a 
fully qualified class name, referring to a custom annotator and/or instances of custom rules class.

Custom annotator and custom rules factory, implemented in `org.rcsb.mojave.tools.jsonschema2pojo` package, 
are used to add `@NotNull` and `@JsonPropertyDescription` annotations attached to getters/setters in addition to default 
annotation at the field level of generated Java types.
