#!/usr/bin/env bash

sbt clean coverage test it:test scalafmtCheckAll coverageReport