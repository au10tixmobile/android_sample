# Au10tix SDK Implementation Example - Android

## Table of Contents
- [Overview](#overview)
- [Usage](#usage)
    - [Artifactory password](#artifactory-password)
    - [JWT token](#jwt-token)
- [Change log](#change-log)

## Overview
Verified, compliant and fraud-free onboarding results in eight seconds (or less). By the time you read this sentence, AU10TIX will have converted countless human smiles, identity documents and data points into authenticated, all-access passes to your products, services and experiences.

This example application presents an implementation suggestion for the Au10tix Mobile SDK.

The following integration examples are included:
- Active Face Liveness
- Passive Face Liveness
- Smart Document Capture
- Proof of Address
- UI components integration
- Sending results to the backend.

## Usage

To use this sample, edit the sample files according to the following steps.

### Artifactory password
1. Contact support for a password to get the artifacts you need. 
1. Modify the project's build.gradle after you receive the password. 

```
password "***CONTACT_SUPPORT_FOR_PASSWORD***"
```

### JWT token
The SDK is prepared using the JWT token produced by the client's server.
Acquire the JWT token and modify Au10NetworkHelper.java to correctly include the values attained from your server.

```java
public class Au10NetworkHelper {
    public static final String JWT_for_Bearer = "xxx.xxx.xxx";
```

## Change log
See [Change log](changelog.md) page for more details

