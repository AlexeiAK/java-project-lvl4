# Page analyzer
[![Actions Status](https://github.com/AlexeiAK/java-project-lvl4/workflows/hexlet-check/badge.svg)](https://github.com/AlexeiAK/java-project-lvl4/actions)
![Java CI](https://github.com/AlexeiAK/java-project-lvl4/actions/workflows/main.yml/badge.svg)
[![Maintainability](https://api.codeclimate.com/v1/badges/65945fb8448e269ba5d5/maintainability)](https://codeclimate.com/github/AlexeiAK/java-project-lvl4/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/65945fb8448e269ba5d5/test_coverage)](https://codeclimate.com/github/AlexeiAK/java-project-lvl4/test_coverage)

[Link to this project on Heroku](https://page-analyzer.herokuapp.com/)  

This is a website that analyzes specified pages for SEO-appropriateness.
Project based on Javalin framework. Based on MVC-architecture: working with routing, query handlers and templates, interacting with the database via ORM.   
In development environment (local) used H2 database, in production environment (on Heroku) used Postgresql.

* Javalin
* Ebean
* Thymeleaf
* Bootstrap
* Postgresql

### How to deploy locally:
```bash
git clone https://github.com/Mr-XEN/java-project-lvl4.git
cd java-project-lvl4
make start-dev # see Makefile for details
# and go to localhost:5000 in browser
```
