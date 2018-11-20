# TeamSyncer.java
This is TeamSyncer (C#) ported to Java.

## Purpose & History
TeamSyncer is a command line tool originally created for syncing members and their roles from one RTC Project or Team Area to other PAs/TAs.

The C# version (Visual Studio) later was extended to be able to sync
- between different Jazz servers as well as different Jazz applications RTC, RDNG and RQM
- from/to AD (LDAP) groups

Additional to porting to Java this version was extended to be able to sync
- from/to Artifactory groups
- from/to GitLab groups

## Configuration

Everything is basically controlled by a XML mapping file, specified with command line parameter --map

Syntax and examples are specified in file [syncer_map_template_v7.xml](https://github.com/jazz-community-rs/TeamSyncer.java/blob/master/test_conf/syncer_map_template_v7.xml).

## Technical Notes / Quirks

Due to the originally limited scope + later need to stay compatible with every extension e.g. concerning the map file syntax, some (coding) internals now look a bit suboptimal.

In particular there is no elaborate account and password handling, credentials (usually admin rights necessary) are hard-coded or built at run time, this code parts have been removed.
At R&S TeamSyncer is a non-public strictly internally used tool, so this was regarded to be sufficient.

The places to look for are tagged "user-specific", affected files:

- Credentials: Program.java, LdapController.java, GitLabAccessObject.java, ArtifactoryGroupHandler.java
- Different access to RDNG (by OSLC instead of Plain Java API): RepoManager.java

OSLC vs. Plain Java API: The C# version used OSLC for accessing Jazz applications. In Java it seemed more appropriate to change to Plain Java API, which worked well for RTC and RQM.
For RDNG Plain Java API login somehow did not work together with Kerberos and as IBM was not able or willing to resolve that, OSLC access is still used for this. Thus the quirky switch in RepoManager.java.

## Used Java Packages (Dependencies)
- Jazz Plain Java API v 6.0.5 (not uploaded)
- `<groupId>org.gitlab4j</groupId>
  <artifactId>gitlab4j-api</artifactId>
  <version>4.8.9</version>`
-   `<groupId>org.jfrog.artifactory.client</groupId>
  <artifactId>artifactory-java-client-services</artifactId>
  <version>2.6.2</version>`
-   `<groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.8.2</version>`

- Above three packages contained in folder 'external' together with all child jars

## Licensing

Copyright (c) Rohde & Schwarz GmbH & Co. KG. All rights reserved.

Licensed under the [MIT](LICENSE) License.