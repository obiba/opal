# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.    

'''
This module contains a script for merging java properties 
files for GWT internationalisation with UIBinder. For more
information on the motivation for this script, see:
  http://code.google.com/p/google-web-toolkit/issues/detail?id=4355
  

Invoke in this way:
   mergelocales Extras_dir LocalizableResource_dir
   
For example:
   mergelocales ./extras/myproject ./src/com/google/gwt/i18n/client/  
  
  
The typical use case:
  - You use internationalisation markup directly in your UIBinder 
    xml files
  - You may or may not use Constants or Messages resources within 
    your java files
  - You want to keep all your translations in a central file
  
The required setup:
  - All your use of the <ui:msg> markup in the xml file MUST contain 
    a description, otherwise you risk getting unexpected DEPRECATED 
    messages. 
  - You need the file 
      src/com/google/gwt/i18n/client/LocalizableResource.properties
    This file will contain all the default locale translations. It 
    must be named exactly this (see link above for details).
  - In the same directory you need 
      LocalizableResource_xxxx.properties 
    for each locale you want to support. For example, xxxx should be 
    fr if you want to translate to french.
  - You need to GWT-compile your project with the "-extra" flag to 
    generate extra files, including UIBinder translations. 
    For example: "-extra extras"
  - If you use Constant or Message to define translations that are 
    used within your java code but not within UIBinder, then you 
    should add a non-UIBinder section to your LocalizableResource 
    file. This sections should begin with:
        ### NON-UIBINDER TRANSLATIONS
    And the following lines should contain all the non-UIBinder 
    translations.
  
The result:
  - The LocalizableResource.properties file will be updated in the 
    following way:
     - It will contain all the UIBinder translations found.
     - It will contain all the non-UIBinder translations that were 
       there before.
     - It will contain all the UIBinder translations that were there 
       before but that were not found within UIBinder files. These 
       will be noted "# TODO: DEPRECATED (CONSIDER REMOVING)".
  - Each locale-specific LocalizableResource_xxxx.properties files 
    will be updated:
     - They will contain all the keys found in UIBinder files, 
       together with their old translation if available or with their 
       default locale text and the comment "# TODO: TRANSLATE" if no 
       translation exist. If some of these have new descriptions, a
       comment "# TODO: CONFIRM TRANSLATION (DESCRIPTION CHANGED)" 
       will be added to the translation. 
     - It will contain all the non-UIBinder keys that were found in 
       LocalizableResource.properties, together with their old 
       translation if available or with their default locale text and 
       the comment "# TODO: TRANSLATE" if no translation exist. 
     - It will contain all the UIBinder translations that were there 
       before but that were
       not found within UIBinder files. These will be noted 
       "# TODO: DEPRECATED (CONSIDER REMOVING)".
  
The .properties files in LocalizableResource_dir will be overwritten.
Although the new files should contain all the translations that were
on the original, be on the safe side and backup your files before
invoking this script. Needless to say, this script comes with no 
warranty whatsoever, but please feel free to contact me if you have
any questions, if you found bugs, or simply if you found the script 
useful.

Created on 2010-02-25

@author: Philippe Beaudoin  (philippe.beaudoin@gmail.com)
'''

import os, re, copy

import sys
import getopt

nonUIBinderComment = '### NON-UIBINDER TRANSLATIONS\n'
deprecatedComment  = '# TODO: DEPRECATED (CONSIDER REMOVING)\n'
translateComment   = '# TODO: TRANSLATE\n'
confirmComment     = '# TODO: CONFIRM TRANSLATION (DESCRIPTION CHANGED)\n'

deprecatedCommentIssued = False
translateCommentIssued = False
confirmCommentIssued = False

def main():
    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], "h", ["help"])
    except getopt.error, msg:
        print msg
        print "for help use --help"
        sys.exit(2)
    # process options
    for o, _ in opts:
        if o in ("-h", "--help"):
            print __doc__
            sys.exit(0)
    if len(args) != 2:
        print "Needs exactly two arguments."
        print "for help use --help"
    
    # process arguments
    mergeLocales( args[0], args[1] )


class InvalidProperty(Exception):
    '''
    Exception raised when a property cannot be properly parsed from a text file.
    '''
    def __init__(self, value):
        self.value = value
        
    def __str__(self):
        return repr(self.value)

class Property(object):
    '''
    A property is a single element of translation. 
    '''
    
    def __init__(self, nonUIBinder):
        '''
        Initialises a new property.
        '''
        self._comments = None
        self._key = None
        self._value = None
        self._nonUIBinder = nonUIBinder
        
    def getFromFile(self, file):
        '''
        Get the next property from a file. Throws a InvalidProperty exception
        if the property is not correctly formatted. This method will replace the
        comments, key and value of this Property object.
        
        @param file: The opened file object to read from.
        @return: True on success, False on EOF. 
        '''
        
        # Skip blank lines. Return false if EOF is reached
        line = readNonBlankLine(file)
        if not line:
            return False

        # Read the comment block
        self._comments = ''
        while line.startswith('#'):
            self._comments += line
            line = file.readline()

        # Read the key/value
        index = line.find('=')
        if index < 0 :
            # Comment-only property, that's fine, may be non-UIBinder though
            if self._comments == nonUIBinderComment :
                self._nonUIBinder = True
            return True
        
        self._key = line[:index].strip()
        if len(self._key) == 0:
            raise InvalidProperty( 'Invalid key found. File: %s.\nLine: %sComment block: %s' % ( file.name, line, self._comments ) )
            
        self._value = line[index+1:]
        while self._value.endswith('\\\n'):
            self._value += file.readline()
        
        return True
    
    def isDeprecated(self):
        '''
        Check if this property is marked as deprecated.
        
        @return: True if it is marked as deprecated, false otherwise.
        '''
        return self._comments.find( deprecatedComment ) >= 0
    
    def setDeprecated(self):
        '''
        Ensures that this property is marked as deprecated, by including an appropriate comment.
        '''
        if not self.isDeprecated():
            self._comments += deprecatedComment
        global deprecatedCommentIssued 
        deprecatedCommentIssued = True
        
    def unsetDeprecated(self):
        '''
        Ensures that this property is not marked as deprecated, by removing any such comment.
        '''
        while self.isDeprecated():
            index = self._comments.find( deprecatedComment )
            self._comments = self._comments[:index] + self._comments[index+len(deprecatedComment):]
        
    def setTranslationNeeded(self):
        '''
        Ensures that this property indicates that it requires translation, by including an appropriate comment.
        '''
        if self._comments.find( translateComment ) < 0:
            self._comments += translateComment
        global translateCommentIssued 
        translateCommentIssued = True
            
    def setConfirmTranslation(self):
        '''
        Ensures that this property indicates that its translation should be confirmed, by including an appropriate comment.
        '''
        if self._comments.find( confirmComment ) < 0:
            self._comments += confirmComment
        global confirmCommentIssued 
        confirmCommentIssued = True
            
    def commentMatches(self, otherProperty):
        '''
        Check that the comment matches between both properties. The comments are considered to
        match if they are exactly the same when the "# TODO:" comments are removed.
        
        @param otherProperty: The other property with which to compare.
        @return: True if the comment matches, False otherwise.
        '''
        return stripToDoComments(self._comments) == stripToDoComments(otherProperty._comments)  
            
    def __str__(self):
        result = self._comments
        if self._key is not None:
            result += self._key + '=' + self._value        
        return result + '\n' 

class PropertyCollection(object):
    '''
    A collection of Property object that can be merged or written to files 
    '''
    
    def __init__(self):
        '''
        Initializes a new collection of Property objects.
        '''
        self._properties = []
        self._map = {}
        self._indexNonUIBinder = None
    
    def add(self, property):
        '''
        Adds an object of type Property to the collection
        
        @param property: The Property object to add
        '''
        if property._key is not None:
            self._map[ property._key ] = property
        if property._nonUIBinder:
            if self._indexNonUIBinder is None:
                self._indexNonUIBinder = len(self._properties)
            self._properties.append( property )
        else:
            if self._indexNonUIBinder is None:
                self._properties.append( property )
            else:
                self._properties.insert(self._indexNonUIBinder, property)
        
    
    def mergeWith(self, otherCollection, markDeprecated, markTranslation ):
        '''
        Merge this collection with another one. Any key that is found in the other collection but not
        in this one will be added. If the markDeprecated parameter is true, any key that is found in 
        this collection but not in the other one will be marked as deprecated. If the markTranslation
        parameter is true, comments will be added to indicate translations that need to be performed.
        
        @param otherCollection: The collection to merge into this one.
        @param markDeprecated: A boolean. True if deprecated translations should be indicated, false otherwise.
        @param markTranslation: A boolean. True means that "# TODO: TRANSLATE" and "# TODO: CONFIRM TRANSLATION" comments
                                will be added when needed.
        '''
        
        # Bring properties over
        for property in otherCollection._properties:
            if property._key is None:
                continue   # Don't merge comment-only properties
            if not self._map.has_key(property._key):
                propertyCopy = copy.copy(property) 
                self.add( propertyCopy )
                if markTranslation:
                    propertyCopy.setTranslationNeeded()
            elif property._comments != '':
                # Empty comments mean non-UIBinder translations OR deprecated translations, skip them.
                # Non-empty comments are copied over, with a confirmation comment if requested.
                myProperty = self._map[property._key]
                if not myProperty.commentMatches( property ):
                    myProperty._comments = property._comments
                    if markTranslation:
                        myProperty.setConfirmTranslation()

        if not markDeprecated:
            return
        
        # Mark deprecated properties
        for property in self._properties:
            if property._key is None:
                continue   # Don't consider comment-only properties
            if not otherCollection._map.has_key(property._key):
                property.setDeprecated()
            elif not property._nonUIBinder:
                # Tricky. Properties that have been removed from all UIBinder files will be
                # found in the resource file, but without comment. (Properties found in UIBinder
                # all have comments because we _require_ that the user provides a description.)
                otherProperty = otherCollection._map[property._key]
                if otherProperty._comments == '' or otherProperty.isDeprecated():
                    property.setDeprecated()

    def __str__(self):
        result = ''
        for property in self._properties:        
            result += str(property)
        return result
        
def stripToDoComments(comment):
    '''
    Remove all the # TODO: comments from the passed comment, returns the result.
    
    @param comment: The comment from which to strip the TODO comments.
    @return: The stripped result.
    '''
    commentsToStrip = [ deprecatedComment, translateComment, confirmComment ]

    for commentToStrip in commentsToStrip:
        length = len(comment)
        index = comment.find(commentToStrip)
        while index != -1:
            comment = comment[:index] + comment[index+length:]
            index = comment.find(commentToStrip)
            
    return comment
        
def readNonBlankLine(file):
    '''
    Eats all the blank lines from a file. A line is blank if it only contains spaces
    followed by a new line.
     
    @param file: The opened file to eat blanks from.
    @return: The first non-blank line read. An empty string if EOF is reached.
    '''
    
    line = '\n'
    while re.match( r'\s*\n', line, re.L ):
        line = file.readline()
    return line


def enumeratePropertyFiles( path ):
    '''
    Looks in the specified directory for all property files, that is, files ending in .properties.
     
    @param path: The directory to look in.
    @return: A list of files.
    '''

    return filter( lambda filename: filename.endswith('.properties'),
                   os.listdir(path) ) 

def readPropertiesFromFile( filename ):
    '''
    Read all the properties from a given file.
    
    @param filename: The name of the file to read properties from.
    @return: A property collection
    '''
    
    file = open(filename, 'r')
    
    try:
        properties = PropertyCollection()
        nonUIBinder = False
        while(True):
            property = Property(nonUIBinder)
            result = property.getFromFile( file )
            if not result:
                break
            property.unsetDeprecated()
            properties.add(property)
            if property._nonUIBinder :
                nonUIBinder = True;
    finally:
        file.close()
        
    if not nonUIBinder:
        # Add an empty non-UIBinder section
        property = Property(True)
        property._comments = nonUIBinderComment
        properties.add(property)
    
    return properties
    
def findLocale( filename ):
    '''
    Identifies the locale given the filename of a property file.
    For example file_fr.properties will return fr.
    If there is no locale in the filename, returns the empty string.
    
    @param filename: The filename from which to extract the locale.
    @return: The locale or the empty string for the default locale.
    '''
    locale = ''  # Default locale when none is found
    match = re.match( r'[^_]*_([^\.]*)\.properties', filename )
    if match:
        locale = match.group(1)
    return locale
         
    
def mergeLocales( extrasDir, resourcesDir ):
    '''
    The main process of this script.
    
    Takes every property file in extrasDir and merge them to the 
    default locale file in resourcesDir. Then it merges this resource file with every other
    non-default local file in the directory.
    '''
    extrasDir = os.path.abspath( extrasDir )
    resourcesDir = os.path.abspath( resourcesDir )
    extraFiles = enumeratePropertyFiles( extrasDir )
    incomingProperties = PropertyCollection()
    for filename in extraFiles:
        locale = findLocale( filename )
        if locale != '':
            print( "Skipping non-default locale in extra directory: " + filename )
            continue
        pathname = os.path.join( extrasDir, filename )
        print( "Processing translations in: " + pathname )
        newProperties = readPropertiesFromFile( pathname )
        incomingProperties.mergeWith( newProperties, False, False )
        
    resourceFiles = enumeratePropertyFiles( resourcesDir )
    defaultLocaleFilename = None    
    for filename in resourceFiles:
        if findLocale( filename ) == '':
            if defaultLocaleFilename is not None :
                print( "Found multiple default locale resources. Using: %s  (Disregarding: %s)" % (defaultLocaleFilename, filename) )
                continue
            defaultLocaleFilename = filename
        
    pathname = os.path.join( resourcesDir, defaultLocaleFilename )
    print( "Merging all translations into default locale: " + pathname )
    defaultLocaleProperties = readPropertiesFromFile( pathname )
    defaultLocaleProperties.mergeWith( incomingProperties, True, False )
    file = open(pathname, 'w')
    try:
        file.write( str(defaultLocaleProperties) )
    finally:
        file.close()
    
    for filename in resourceFiles:
        if findLocale( filename ) != '':
            pathname = os.path.join( resourcesDir, filename )
            print( "Merging all translations into non-default locale: " + pathname )
            otherLocaleProperties = readPropertiesFromFile( pathname )
            otherLocaleProperties.mergeWith( defaultLocaleProperties, True, True )
            file = open(pathname, 'w')
            try:
                file.write( str(otherLocaleProperties) )
            finally:
                file.close()
    
    if deprecatedCommentIssued:
        print( "Deprecated translations found. Look for: '%s'." % deprecatedComment.strip() )
    if confirmCommentIssued:
        print( "Some translations could require confirmation. Look for: '%s'." % confirmComment.strip() )
    if translateCommentIssued:
        print( "Some properties need to be translated. Look for: '%s'." % translateComment.strip() )
         
    
if __name__ == "__main__":
    main()    
    