#!/bin/bash

/root/.nvm/versions/node/v8.9.4/bin/chrome-headless-render-pdf --url file://$1 --pdf $2 --chrome-option=--no-sandbox

