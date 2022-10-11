/*
 October 2022 mika.nokka1@gmail.com
 
 Project has several security level settings, default is the most restricted onbe. When user not having rights
 for default settings creates ticket, visibility disappears. This postfunction sets newly created issue 
 security according user group (if not in default rights group). Correct users are in two defined groups (only)
 
 For Jira Data Center usage
 */

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.fields.CustomField

import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;

import com.atlassian.jira.issue.security.IssueSecurityLevelManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.IssueInputParametersImpl
import com.atlassian.jira.event.type.EventDispatchOption

def userManager=ComponentAccessor.getUserManager()
def loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
def issueManager= ComponentAccessor.issueManager
def issueService = ComponentAccessor.issueService

def groupManager = ComponentAccessor.groupManager
def securityLevelManager = ComponentAccessor.getComponent(IssueSecurityLevelManager)



// CONFIGURATIONS
def group1="xxx" // first required  group for correct user indentification
def group2="yyy" // second required  group for correct user indentification
def groupsCounter=0  // counter for allowed and required group partisipation
def allowedGroupnsNmbr=2 // max number of groups for the user
final String commenttext = "FYI:Issue security setting was changed from default by automation"
final boolean dispatchEvent = true // send event after commenting
//


// set logging to Jira log
def log = Logger.getLogger("IssueSecuritySetter") // change for customer system
log.setLevel(Level.DEBUG)  // DEBUG INFO
 
log.debug("---------- IssueSecuritySetter started -----------")


def util = ComponentAccessor.getUserUtil()
whoisthis=ComponentAccessor.getJiraAuthenticationContext().getUser()
log.debug("Issue: {$issue} --> Script run as a user: {$whoisthis}")

def groupNames = groupManager.getGroupNamesForUser(whoisthis)
def numberOfGroups=groupNames.size()

log.debug("User's groups {$numberOfGroups}: {$groupNames}")


// in POC case, user with limited access, only in two user groups
if  (ComponentAccessor.groupManager.isUserInGroup(whoisthis, group1)) {
	log.debug("User IS in group: {$group1}")
	groupsCounter=groupsCounter+1
}
else {
	log.debug("User is NOT in group: {$group1}")
}


if  (ComponentAccessor.groupManager.isUserInGroup(whoisthis, group2)) {
	log.debug("User IS in group: {$group2}")
	groupsCounter=groupsCounter+1
}
else {
	log.debug("User is NOT in group: {$group1}")
}


// check if security settings is needed to be done
if(allowedGroupnsNmbr==groupsCounter) {
	log.debug("User is only allowed groups {$allowedGroupnsNmbr}:  $group1 , $group2")
	log.debug("==> Going to do issue security settings change")
	
	def securityLevelId = issue.securityLevelId
	def securityLevelName=securityLevelManager.getIssueSecurityName(securityLevelId)
	log.debug("Current Issue Security: ${securityLevelName}")
	
	//use account having rights to do the security settings
	
	// capture context of original user
	def authContext = ComponentAccessor.getJiraAuthenticationContext()
	
	//try and assert, maybe too much
	try{
		
		 authContext.setLoggedInUser(ComponentAccessor.getUserManager().getUserByName("mika.nokka")); // use script account
		 
		 // force sets IssueSecurityLevel to this ID 
		 def issueSecurityLevelId=10200 //system spesific level id from Jira UI
		 def issueInputParameters = new IssueInputParametersImpl()
		 issueInputParameters.setSecurityLevelId(issueSecurityLevelId)

		 def updateValidationResult = issueService.validateUpdate(loggedInUser, issue.id, issueInputParameters)
		 assert updateValidationResult.valid : updateValidationResult.errorCollection

		 def issueUpdateResult = issueService.update(loggedInUser, updateValidationResult, EventDispatchOption.ISSUE_UPDATED, false)
		 assert issueUpdateResult.valid : issueUpdateResult.errorCollection
		 log.debug("Force set Issue($issue) Security id as: ${issueSecurityLevelId}")
		
		}catch(Exception e){
		 e.printStackTrace();
		}finally {
		 log.error("ERROR: SCript failed to change user for issue security operation")
		 authContext.setLoggedInUser(whoisthis);
		}
	

	authContext.setLoggedInUser(whoisthis);
	ComponentAccessor.commentManager.create(issue, whoisthis, commenttext, dispatchEvent)
	
}
else if ( (numberOfGroups > allowedGroupnsNmbr) || (groupsCounter !=  allowedGroupnsNmbr))     {
	log.debug("ERROR: User is listed in more than allowed groups: {$numberOfGroups}.  (allowed:{$allowedGroupnsNmbr}")
	log.debug("ERROR: Or user is not member of all needed groups" )
	log.debug("==> Not going to do any issue security settings")
}




log.debug("---------- IssueSecuritySetter stopped -----------")