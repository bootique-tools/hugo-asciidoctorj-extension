AsciidoctorJ post processor, that extracts ToC into separate file and optionally can inject content into rendered document. 
Can be used only for HTML backend, will **fail** if used with PDF.

It is targeted to inject `front-matter` section suitable for the Hugo website generator.

Extension controlled by attributes in `*.adoc` file:
- `hugo-header`: header file name, if not set empty 'front-matter' will be used
- `hugo-font-awesome-icons`: convert icons to a FontAwesome compatible variant (`true`/`false`, default is `true`)
- `hugo-multipage`: split document to a separate pages (`true`/`false`, default is `false`)
- `hugo-multipage-level`: nested section level that will be used for a single page content in a multipage documents (default is `1`)
- `hugo-multipage-header`: header file name to add to a multipage documents, index page will always use `hugo-header`, if not set empty 'front-matter' will be used
- `hugo-multipage-ref`: reference prefix for a multipage navigation