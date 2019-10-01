# Reefer Container Shipment Order Management

This project is one reference implementation of the CQRS and event sourcing patterns as part of the [Event Driven Architecture](https://github.com/ibm-cloud-architecture/refarch-eda) reference architecture. From a use case point of view, it implements the order subdomain of[Reefer Container shipment process](https://github.com/ibm-cloud-architecture/refarch-kc). This repository aims to support the order management to ship fresh cargo from the manufacturer to the final destinatio. The business process is defined [here](https://ibm-cloud-architecture.github.io/refarch-kc/introduction/).

For better reading experience go to [the book view here.](http://ibm-cloud-architecture.github.io/refarch-kc-order-ms)

---

### Building this booklet locally

The content of this repository is written with markdown files, packaged with [MkDocs](https://www.mkdocs.org/) and can be built into a book-readable format by MkDocs build processes.

1. Install MkDocs locally following the [official documentation instructions](https://www.mkdocs.org/#installation).
1. Install Material plugin for mkdocs:  `pip install mkdocs-material` 
2. `git clone https://github.com/ibm-cloud-architecture/refarch-kc.git` _(or your forked repository if you plan to edit)_
3. `cd refarch-kc`
4. `mkdocs serve`
5. Go to `http://127.0.0.1:8000/` in your browser.

### Pushing the book to GitHub Pages

1. Ensure that all your local changes to the `master` branch have been committed and pushed to the remote repository.
   1. `git push origin master`
2. Ensure that you have the latest commits to the `gh-pages` branch, so you can get others' updates.
	```bash
	git checkout gh-pages
	git pull origin gh-pages
	
	git checkout master
	```
3. Run `mkdocs gh-deploy` from the root refarch-kc directory.

--- 

## Contribute

As this implementation solution is part of the Event Driven architeture reference architecture, the [contribution policies](./CONTRIBUTING.md) apply the same way here.

**Contributors:**

* [Jerome Boyer](https://www.linkedin.com/in/jeromeboyer/)
* [Edoardo Comar](https://www.linkedin.com/in/edoardo-comar/)
* [Jordan Tucker](https://www.linkedin.com/in/jordan-tucker-ba328a12b/)
* [Mickael Maison](https://www.linkedin.com/in/mickaelmaison/)
* [Francis Parr](https://www.linkedin.com/in/francis-parr-26041924)

