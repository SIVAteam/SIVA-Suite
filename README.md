# SIVA Suite
The SIVA Suite is an open source framework for the creation, playback and administration of hypervideos. It is made up of three components. An authoring tool (SIVA Producer), an HTML5 based hypervideo player (SIVA Player) and a server component for user and content management (SIVA Server).

Feel free to take a look at a [demo](http://siva.uni-passau.de/mirkul/projekte/BB_Trainer) video showcasing the SIVA Player as well as some of the features of hypervideos created with the SIVA Producer.

## Installation & licensing
The SIVA Producer is released as a single setup file. The installation follows the standard procedure for Windows applications offering an installation wizard. SIVA Player is included in the SIVA Producer and exported alonside each of the created hypervideos. Therefore, no installation process is required and the player can be accessed directly from the export directory of a hypervideo by opening its *index.html*. For the SIVA Server, installation instructions can be found in the readme files of its subcomponents ([server application](./server/serverApplication/README.txt), [player stats](./server/playerStats/README.txt)).

The SIVA Producer is licensed under the terms of the [Eclipse Public License v1](https://www.eclipse.org/legal/epl-v10.html). More details on its license can be found [here](./producer/about.html). The SIVA Player as well as the SIVA Server are licensed under the [GNU General Public License v3](http://www.gnu.org/copyleft/gpl.html).


## How to build SIVA Producer
The SIVA Producer is a 32-bit Java 1.6 application based on the Eclipse Rich Client Platform. As such, building it from source code requires a 32-bit [Eclipse for RCP and RAP developers](http://www.eclipse.org/downloads/) platform and a compatible Java environment to be installed on the build system. Within Eclipse, the build process comprises three steps:
- Import the source code into the Eclipse workbench
- Set the correct target platform to compile against
- Export the SIVA Producer as an RCP product

In the following we will describe in detail how to perform these steps from within Eclipse for RCP and RAP developers version 4.4.2.

### Import sources from GitHub
- Upon starting Eclipse you first have to choose a workspace directory where project information will be stored.  
- After closing the welcome tab, open up the *File* menu and select *Import...*.  
- In the import dialog look for a *Git* folder, select *Projects from Git* and click *Next*.  
- Choose *Clone URI* and on the next page enter https://github.com/SIVAteam/SIVA-Suite as URI and continue.  
- After selecting the branch to pull (usually master), you need to select a directory to clone the repository to. A click on *Next* will start the download process. **Note**: You don't have to select the same directory as for the workspace.
- When the download is finished, ensure *Import existing projects* is selected and click on *Next*.
- On the last dialog page keep all found projects selected and *Finish* the import.

### Set target platform
- In the Package Explorer shown on the left, you might notice red signs indicating compile errors. To be able to compile the SIVA Producer, the correct target platform has to be set first. To do so, in the Package Explorer navigate to the project *org.iviPro.target*.
- Open the file *target-definition-3.8.2.target* with a double click. An editor with the title *Target Definition* should pop up. In the upper right corner of that editor click the link *Set as Target Platform*. After a few seconds, the red error signs should disappear. **Note:** If only a text editor without the mentioned link pops up, make sure you are using an RCP version of Eclipse.

Right now you can run the SIVA Producer from within the Eclipse environment. If you want to do so, right click *org.iviPro.ui* in the Package Explorer and select *Run as...*, *Eclipse Application*. You will be prompted to select your desired language configuration and are ready to go. However, if you want to know how to create a standalone Windows application, carry on with the next steps.

### Export as RCP product
- Right click on *org.iviPro.ui* in the Package Explorer and select *Export...*.
- Inside *Plug-in Development* select *Eclipse product* and click *Next*.
- Be sure to uncheck *Generate p2 repository* first. Afterwards choose a root and a destination directory. The root directory will be put inside the destination directory and will contain the application files.

Unfortunately the export wizard does not allow for the automatic extraction of supplementary files and libraries directly to the selected root directory. Therefore, licensing information and native libraries have to be copied manually.
- In the Package Explorer navigate into *org.iviPro.ui*. Select *libs-native*, *LICENSES* and *about.html*. Right click on your selection and select *Copy*. Open your file explorer and navigate to the root directory you chose during the export process. The correct directory will contain an executable file named *SIVA Producer.exe*. After inserting the copied selection into the root directory, you are done. You should now be able to run the SIVA Producer directly from the file explorer.
