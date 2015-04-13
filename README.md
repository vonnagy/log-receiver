log-receiver
===========================

[![Build Status](https://travis-ci.org/vonnagy/log-receiver.png?branch=master)](https://travis-ci.org/vonnagy/log-receiver)

This is a service that receives logs from Heroku drains and forwards them on to storage or processors. It is written in Scala, 
which can easily be deployed to Heroku.  

This application support the [Getting Started with Scala on Heroku](https://devcenter.heroku.com/articles/getting-started-with-scala) article - check it out.

## Running Locally

Make sure you have Scala and sbt installed.  Also, install the [Heroku Toolbelt](https://toolbelt.heroku.com/).

```sh
$ git clone https://github.com/heroku/log-receiver.git
$ cd log-receiver
$ sbt compile stage
$ foreman start web
```

Your app should now be running on [localhost:5000](http://localhost:5000/).

## Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```

## Documentation

For more information about using Scala on Heroku, see these Dev Center articles:

- [Scala on Heroku](https://devcenter.heroku.com/categories/scala)

