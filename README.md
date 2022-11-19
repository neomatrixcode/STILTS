# STILTS
The STIL Tool Set is a set of command-line tools based on STIL, taken out http://www.star.bris.ac.uk/~mbt/stilts/

Version 3.4-7 released 5 October 2022


## What is STILTS?
The STIL Tool Set is a set of command-line tools based on STIL, the Starlink Tables Infrastructure Library. It deals with the processing of tabular data; the package has been designed for, but is not restricted to, astronomical tables such as source catalogues. Some of the tools are generic and can work with multiple formats (including FITS, VOTable, CDF, ECSV, CSV, PDS4, Parquet, MRT, Feather, GBIN, SQL and ASCII), and others are specific to the VOTable format. STILTS is the command-line counterpart of the GUI table analysis tool TOPCAT. The package is robust, fully documented, and designed for efficiency, especially with very large datasets.

Facilities offered include:

- format conversion
- crossmatching
- plotting
- column calculation and rearrangement
- row selections
- data and metadata manipulation and display
- sorting
- statistical calculations
- histogram calculation
- data and service validation
- access to remote data services including Virtual Observatory

A powerful and extensible expression language is used for specifying data calculations. These facilities can be put together in very flexible and efficient ways.
For tasks in which the data can be streamed, the size of table STILTS can process is effectively unlimited. For other tasks, tables of a few million rows and a few hundred columns usually do not present a problem.

STILTS is written in pure Java (Java SE 8 or later; versions prior to STILTS 3.2 were Java SE 6), and can be run from the command line or from Jython, or embedded into java applications. The STILTS application is released under the GNU GPL, though much of the library code is licensed under the LGPL or other less restrictive licenses - see LICENSE.txt.

## Commands
STILTS currently consists of twelve generic table processing commands:

- tcopy - Table format converter
- tpipe - Generic table pipeline processing utility
- tmatch2, tskymatch2 - Two-table crossmatchers
- tmatch1 - Intra-table crossmatcher
- tmatchn - Multi-table crossmatcher
- tjoin - Trivial side-by-side multiple-table joiner
- arrayjoin - Adds table-per-row data as array-valued columns
- tgridmap - N-dimensional histogram calculator with table output
- tcube - N-dimensional histogram calculator with FITS array output
- tcat, tcatn - Multiple-table concatenaters
- tmulti, tmultin - Multiple-table container writers
- tloop - Test table creation utility

five plotting commands (as well as three deprecated ones):

- plot2plane - Plots on 2d Cartesian axes
- plot2sky - Plots on celestial axes
- plot2cube - Plots on 3d Cartesian axes
- plot2sphere - Plots in spherical polar space
- plot2time - Plots 2-d axes with Time horizontally

two VOTable-specific commands:

- votcopy - VOTable encoding translator
- votlint - VOTable validity checker

nine Virtual Observatory/external data service access commands:

- cone - Cone-search like queries (including SIA and SSA)
- tapquery, tapresume, tapskymatch - TAP service clients
- cdsskymatch - match local table against VizieR/SIMBAD using CDS X-Match service
- taplint - TAP service test suite
- datalinklint - DataLink validator
- regquery - Registry Query
- coneskymatch (formerly multicone, now somewhat deprecated) - Match local table with one behind a Cone Search/SIA/SSA service

three sky pixel-related commands:

- tskymap - Generate HEALPix sky density maps
- pixfoot - Generate Multi-Order Coverage (MOC) maps
- pixsample - Sample from a HEALPix pixel data file

three SQL-specific commands:

- sqlskymatch (formerly sqlcone) - Match local table with one in an SQL database
- sqlclient - JDBC-based SQL command-line client
- sqlupdate - Updates data in existing cells of an RDBMS table

and some miscellaneous items:

- server - HTTP server which executes STILTS commands
- calc - Quick expression evaluator
- funcs - Documentation browser for expression language functions
- xsdvalidate - Validates against XML schema

See also the Commands by Category section of the manual.

More commands and facilities may be added in the future.

