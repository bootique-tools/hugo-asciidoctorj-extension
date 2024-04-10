AsciidoctorJ post processor, that extracts ToC into separate file and optionally can inject content into rendered document. 
Can be used only for HTML backend, will **fail** if used with PDF.

It is targeted to inject `front-matter` section suitable for the Hugo website generator.

Extension controlled by attributes in `*.adoc` file:
- `hugo-header`: header file name, if not set empty 'front-matter' will be used
- `multipage`: split document to a separate pages
- `multipage-level`: nested section level that will be used for a single page content in a multipage documents
- `multipage-header`: header file name to add to a multipage documents, index page will always use cayenne-header
- `multipage-ref`: reference prefix for a multipage navigation