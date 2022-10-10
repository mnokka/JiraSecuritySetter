/*
 October 2022 mika.nokka1@gmail.com
 
 Project has several security level settings, default is the most restricted onbe. When user not having rights for default settings creates ticket,
 visibility disappear. This postfunction sets newly created issue security according user group (if not in default rights group)
 
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




// CONFIGURATIONS
def group1="xxx"" // first required  group for correct user indentification
def group2="yyy"  // second required  group for correct user indentification
def groupsCounter=0  // counter for allowed and required group partisipation
def allowedGroupnsNmbr=2 // max number of groups for the user
//


// set logging to Jira log
def log = Logger.getLogger("IssueSecuritySetter") // change for customer system
log.setLevel(Level.DEBUG)  // DEBUG INFO
 
log.debug("---------- IssueSecuritySetter started -----------")


def util = ComponentAccessor.getUserUtil()
whoisthis=ComponentAccessor.getJiraAuthenticationContext().getUser()
log.debug("Script run as a user: {$whoisthis}")

def groupNames = groupManager.getGroupNamesForUser(whoisthis)
log.debug("User: {$groupNames}")
def numberOfGroups=groupNames.size()
log.debug("Number of groups: {$numberOfGroups}")


// in POC case, user with limited access, only in two user groups
if  (ComponentAccessor.groupManager.isUserInGroup(whoisthis, group1)) {
	log.debug("User IS in group: {$group1}")
	groupsCounter=groupsCounter+1
}
else {
	log.debug("User is NOT in group: {$group1}")
}


if  (ComponentAccessor.groupManager.isUserInGroup(whoisthis, group2)) {
	log.debug("User IS in group: {$group1}")
	groupsCounter=groupsCounter+1
}
else {
	log.debug("User is NOT in group: {$group1}")
}


if(allowedGroupnsNmbr==groupsCounter) {
	log.debug("User is only allowed groups: {$allowedGroupnsNmbr}")
	log.debug("Going to do issue security settings change")
}
else {
	log.debug("User is listed in more than allowed groups: {$numberOfGroups}.  (allowed:{$allowedGroupnsNmbr}")
	log.debug("Not going to do any issue security settings")
}




log.debug("---------- IssueSecuritySetter stopped -----------")