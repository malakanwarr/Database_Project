# Mini Relational Database Management System (DBMS)

A custom-built, page-based Relational Database Management System (DBMS) developed for the CSEN604 - Data Bases II course at the German University in Cairo. 

This engine is built from scratch to simulate the internal mechanics of a relational database, focusing on data storage, query processing, indexing, and crash recovery. It is structured across three architectural layers: the DBMS level, the Table level, and the Page level.

## 🚀 Key Features

### Core Database Operations (Milestone 1)
* **Data Definition & Manipulation:** Support for creating tables and inserting records across managed memory pages.
* **Query Engine:** Capable of executing flexible `SELECT` operations:
  * Full table scans (Retrieve all data).
  * Conditional selections (Filter by specific column values).
  * Direct pointer access (Retrieve specific records directly from memory).
* **Execution Tracing:** Built-in tracing functionality to monitor and output the history of operations performed on any table, down to the execution time per page.

### Indexing & Recovery (Milestone 2)
* **Bitmap Indexing:** Implemented a Bitmap Index structure to heavily optimize query performance. Includes support for dynamically creating the index, inserting new data into the index, and accelerating `SELECT` statements using bitwise operations.
* **Crash Recovery:** A robust data recovery manager that can validate data integrity, identify missing records, and restore lost data pages to return the database to a consistent state.

## 🛠️ Architecture Details
* **Storage Model:** Page-based storage architecture.
* **Data Types:** For the scope of this engine, all stored data types are normalized as Strings to focus on structural logic and retrieval speed.
* **Index Structure:** Bitmap Indexing for low-cardinality data optimization.

👨‍💻 Author
Malak Amr Anwar Computer Science and Engineering
