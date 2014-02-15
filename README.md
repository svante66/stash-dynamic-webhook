# Stash Dynamic Webhook

After making commits to Stash make HTTP(S) GET requests to URLs based upon rule-sets matching refs in the git changeset.

This allows for making simple regex matches against, say, the branch merged into -- and build a dynamic URL based upon the match.

Specifically, this plugin was developed to allow true CI for Jenkins git projects *without SCM polling*, multi-branch configurations, or creating a build per environment.

## Installation
### Set up the Atlassian Plugin SDK
In order to build this plugin you first need to install the Atlassian Plugin SDK, the documentation for which can be found for Windows, Linux and Mac at:
https://developer.atlassian.com/display/DOCS/Set+up+the+Atlassian+Plugin+SDK+and+Build+a+Project

Upon successful installation, you should have the `atlas-mvn` command in your `$PATH`.

### Compiling the Plugin
Simple execute `atlas-mvn install` from within the main `stash-dynamic-webhook` directory (or wherever you downloaded/cloned this repo to).

### Installing the Stash Plugin
-  Login to Stash with a user account that has admin priviledges
-  Click on the Settings cog in the top-right corner
-  Navigate to Manage Addons
-  Click Upload a new addon...
-  Navigate to the `stash-dynamic-webhook/target` directory and select the `stash-dynamic-webhook-1.0-SNAPSHOT.jar` file

## Setup

Once installed, follow these steps:
-  Navigate to a repository in Stash.
-  Hit the *Settings* link (for the repository)
-  In the left-navigation, hit the *Hooks* link
-  For the **Stash Dynamic Webhook**, click the *Enable* button.
-  Enter your rule-sets, one rule per line.
-  Submit the form.
-  Commit some code and if the commit refs match, a request will be generated!

## Writing Rule-sets

A rule-set is made of two parts. A pattern to match, and a URL to invoke when the pattern matches.

In it's simplist form, the following rule will match any post-receive and invoke the given URL.
```
(.*) http://ci.foobar.com/job/AJob/build
```

A more complex situation, including RegEx back-references to modify the URL could look like this:
```
.*/(release/.*) http://ci.foobar.com/job/AJob/buildWithParameters?token=AToken&branch=$1
```
The above configuration can be combined with a Jenkins build configured for parameters.
All parameters in the URL are URL-encoded to allow branches with special characters passed as 
query parameters.


You can add one match & URL per line. You can create a Jenkins parameterized build and use the same
build for each noteworthy branch in the project and target specific deploy environments.
```
.*/(dev) http://ci.foobar.com/job/AJob/buildWithParameters?token=AToken&branch=$1&deployto=dev
.*/(release/.*) http://ci.foobar.com/job/AJob/buildWithParameters?token=AToken&branch=origin/$1&deployto=qa
.*/(master) http://ci.foobar.com/job/AJob/buildWithParameters?token=AToken&branch=origin/$1&deployto=prod
.*/(hotfix/.*) http://ci.foobar.com/job/AJob/buildWithParameters?token=AToken&branch=origin/$1&deployto=emer
```

## Release Notes
### Version 1.0
-  Initial release
