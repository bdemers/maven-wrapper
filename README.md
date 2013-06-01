This is a copy of the gradle wrapper ported for use with maven.

To run this build as an example run the 'mvnw' script at the root of this repo as you would normally run 'mvn'
For example if you typically run 

    $ mvn clean install
	
Now that the plugin is installed in your Maven cache, you can integrate the wrapper with your existing or new Maven projects.

The wrapper will __always__ use your current Maven version.
A wrapper created with Maven 3.0.4 will request Maven 3.0.4 if it's not already installed on the user's machine.	

In the _build_ section of your pom.xml, add the following :

	<code>
	 <pre>
	    <build>
		 ...
	         <plugins>
			 ...
	      <plugin>
	        <groupId>org.apache.maven</groupId>
	        <artifactId>wrapper-maven-plugin</artifactId>
	        <version>0.0.1-SNAPSHOT</version>
	      </plugin>
		  ...
	    </plugins>
		 ...
	    </build>
	</code>
	</pre>
	
Run the following command to generate the wrapper and supporting files.

    $ mvn wrapper:wrapper

The _mvnw_ and _mwnw.bat_ command wrappers are generated at the root of the project folder.
	
The _maven/wrapper_ folder is generated with the relevant jar and properties file in the project directory.


You can now start using either the __mvnw__ or __mvnw.bat__ commands on any project without an existing maven installation!
