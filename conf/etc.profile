#***********************************************************************
#       Licensed Materials - Property of IBM                           *
#       5647-A01                                                       *
#       (C) Copyright IBM Corp. 1992, 1999                             *
#                                                                      *
#***********************************************************************
# This is a sample profile defining system wide variables. The
# variables set here may be overridden by a user's personal .profile
# in their $HOME directory.
#
# In order to customize this profile, you must first copy this sample
# to /etc/profile. More information on this profile may be found in
# OS/390 UNIX System Services Planning book and the OS/390 UNIX System
# Services User's Guide. This sample does not contain an exhaustive
# list of all variables, but only lists commonly used ones.
#
# To enable and disable lines in this profile you may remove or
# add '#' to uncomment or comment the desired lines.
#
# Variables must be 'exported' in order for the variables to be
# available for subsequent commands.
#
# Some environment variables allow you to concatenate data set names
# or directories. Most use the colon character (':') as a delimiter
# between these names.
#
# Example -   PATH=/bin:/usr/lpp/xxxxxx
#             export PATH
#
# Another method to concatenate and also allow for easier
# management is by using the previous setting of that environment
# variable.
#
# Example -   PATH=/bin
#             PATH=$PATH:/usr/lpp/xxxxxx
#             export PATH
#
#***********************************************************************

# ======================================================================
#                    STEPLIB environment variable
#                    ----------------------------
# Specifies a list of data sets to be searched ahead of the normal
# search order when executing a program. To improve the shell's
# performance for users from ISPF or users with data sets allocated to
# STEPLIB DD statements, specify "STEPLIB=none" .
# This performance improvement is not applicable to non-interactive
# shells, for example those started with the BPXBATCH and OSHELL
# utilities.
#
#    11/13/02  Added SGSKLOAD & SCLBDLL to support GSKKYMAN SSL command
#    05/31/06  Removed SGSKLOAD, library obsolete
# ======================================================================
if [ -z "$STEPLIB" ] && tty -s;
then
#   export STEPLIB=none
#   export STEPLIB=$STEPLIB:SYS2.GSK.SGSKLOAD
#
#   Added by GJR 4/14/2009
#
    export STEPLIB=SYS1.LINKLIBA
    exec sh -L
fi

# ======================================================================
#                      TZ environment variable
#                      -----------------------
# Specifies the local time zone.
# ======================================================================
TZ=EST5EDT
export TZ

# ======================================================================
#                     LANG environment variable
#                     -------------------------
# Specifies the language you want the messages to displayed in.
# For Japanese: LANG=Ja_JP
# ======================================================================
LANG=C
export LANG

# ======================================================================
#                   LOGNAME environment variable
#                   ----------------------------
# This environment variable is set when 'logging' into the shell
# environment. You can avoid accidental modification to this variable
# by making the LOGNAME variable read-only.
# ======================================================================
readonly LOGNAME

# ======================================================================
# Rocket open source tools home directories
# ======================================================================
DBB_HOME=/usr/lpp/IBM/dbb
export DBB_HOME
DBB_CONF=/usr/lpp/IBM/dbb/conf
export DBB_CONF
GROOVY_HOME=/usr/lpp/IBM/dbb/groovy-2.4.12
export JAVA_HOME
JAVA_HOME=/usr/lpp/java/J8.0_64
export JAVA_HOME
TOOLS_HOME=/usr/lpp/tools
export TOOLS_HOME
# ======================================================================
#                     PATH environment variable
#                     -------------------------
# Specifies the list of directories that the system searches for an
# executable command. If you want to include the current working
# directory in your search order, then the enviroment variable would
# be
#   PATH=/bin:.
#
# The current working directory is represented by dot ('.') .
# ======================================================================
# Added for JAVA Development Kit   -- 5/21/01  DJW

PATH=/bin:.
PATH=$PATH:$JAVA_HOME/bin
PATH=$PATH:$TOOLS_HOME/bin
PATH=$PATH:$DBB_HOME/bin
PATH=$PATH:$GROOVY_HOME/bin
export PATH

# ======================================================================
#                    LIBPATH environment variable
#                    ----------------------------
# Specifies the list of directories that the system searches for a DLL
# (Dynamic Link Library) filename. If not set, the current working
# directory is searched.
# ======================================================================
LIBPATH=/lib:/usr/lib
LIBPATH=$LIBPATH:$JAVA_HOME/bin
LIBPATH=$LIBPATH:$JAVA_HOME/lib
LIBPATH=$LIBPATH:$JAVA_HOME/lib/s390x
LIBPATH=$LIBPATH:$JAVA_HOME/lib/s390x/j9vm
LIBPATH=$LIBPATH:$TOOLS_HOME/lib
LIBPATH=$LIBPATH:$TOOLS_HOME/lib/perl5/5.24.0/os390/CORE
LIBPATH=$LIBPATH:$DBB_HOME/lib
LIBPATH=$LIBPATH:$GROOVY_HOME/lib
export LIBPATH

# ===========================================
CLASSPATH=$JAVA_HOME/lib
CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/ext
CLASSPATH=$CLASSPATH:$DBB_HOME/lib
CLASSPATH=$CLASSPATH:$GROOVY_HOME/lib
#
export CLASSPATH
# ======================================================================
IJO="-Xms2M -Xmx1024M"
# Uncomment the following to aid in debugging "Class Not Found" problems
#IJO="$IJO -verbose:class"
# Uncomment the following if you want to run with Ascii file encoding..
#IJO="$IJO -Dfile.encoding=ISO8859-1"
# Uncommnect to Use Java shared classes
IJO="$IJO -Xshareclasses"
export IBM_JAVA_OPTIONS="$IJO "

# ======================================================================
#                    NLSPATH environment variable
#                    ----------------------------
# Specifies the list of directories that the system searches for
# message catalogs (NLS files). The %L represents the language currently
# set by the LANG environment variable, and %N represents the name
# of the message catalog.
# ======================================================================
NLSPATH=/usr/lib/nls/msg/%L/%N
export NLSPATH

# ======================================================================
#                    MANPATH environment variable
#                    ----------------------------
# Specifies the list of directories that the system searches for man
# pages (help files). The %L represents the language currently set by
# the LANG environment variable.
# ======================================================================
MANPATH=/usr/man/%L
MANPATH=$MANPATH:$TOOLS_HOME/man
export MANPATH

# ======================================================================
#                     MAIL environment variable
#                     -------------------------
# Sets the name of the user's mailbox file and enables mail
# notification.
# ======================================================================
MAIL=/usr/mail/$LOGNAME
export MAIL
# ==================================================================
#                     PORTMAP setup
#                     =============
# ==================================================================
# RESOLVER_CONFIG=/etc/resolv.conf
# export RESOLVER_CONFIG
export RESOLVER_CONFIG="//'SYS2.TCPIP.PARMLIB(TCPDATA)'"

# ======================================================================
#                           umask variable
#                           --------------
# Sets the default file creation mask - reference umask in the OS/390
# UNIX System Services Command Reference
# ======================================================================
umask 022

# ======================================================================
# Start of c89/cc/c++ customization section
# ======================================================================
#
#   The following environment variables are used to provide information
#   to the c89/cc/c++ utilities, such as (parts of) data set names which
#   are dynamically allocated.
#
#   If installation of the compiler and/or runtime library elements use
#   different values, then the appropriate "export" lines should be
#   set to the correct value (and uncommented).  Note that since a
#   VOL=SER= paramater is not supported by c89/cc/c++, all named data
#   sets used by c89/cc/c++ must be cataloged.
#
#   It may be necessary to override the default esoteric unit for
#   (unnamed) work data sets, if the c89/cc/c++ default (SYSDA) is not
#   defined for the installed system. A NULL ("") value may be specified
#   in order to allow c89/cc/c++ to use an installation defined default.
#
#   For the _INCDIRS and _LIBDIRS environment variables, use the
#   blank character (' ') as a delimiter when concatenating directories.
#
#   To enable exporting c89/cc/c++ environment variables, uncomment the
#   "for" statement, the "done" statement, and whichever "export"
#   statements need to be customized.  Normally c89, cc and c++ all use
#   the same values, so this "for" loop will cause all of them to be
#   set. To set any particular c89, cc or c++ variable differently,
#   just code the necessary "export" statement (using the appropriate
#   prefix), following the "for" loop.
#
#   Note: This is not an exhaustive list of the environment
#   variables that affect the behavior of c89/cc/c++.  It is however all
#   those that will normally might require customization by the system
#   programmer.  For ease of migration, it is recommended that of these
#   only the variables necessary for correct operation of cc/c89/c++ be
#   set.  Consult the "Environment Variables" section of the c89/cc/c++
#   command in the OS/390 UNIX System Services Command Reference for
#   complete information about these environment variables.
#
# ######################################################################
#
# for _CMP in _C89 _CC _CXX; do
#
#
# High-Level Qualifier "prefixes" for data sets used by c89/cc/c++:
# ======================================================================
#
#
#   C/C++ Compiler:
#   ----------------------------------------
#   export ${_CMP}_CLIB_PREFIX="CBC"
#
#
#   Prelinker and runtime library:
#   ----------------------------------------
#   export ${_CMP}_PLIB_PREFIX="CEE"
#
#
#   OS/390 system data sets:
#   ----------------------------------------
#   export ${_CMP}_SLIB_PREFIX="SYS1"
#
#
# Compile and link-edit search paths:
# ======================================================================
#
#
#   Compiler include file directories:
#   ----------------------------------------
#   export ${_CMP}_INCDIRS="/usr/include /usr/lpp/ioclib/include"
#
#
#   Link-edit archive library directories:
#   ----------------------------------------
#   export ${_CMP}_LIBDIRS="/lib /usr/lib"
#
#
# Esoteric unit for data sets:
# ======================================================================
#
#
#   Unit for (unnamed) work data sets:
#   ----------------------------------------
#   export ${_CMP}_WORK_UNIT="SYSDA"
#
#
# done; unset _CMP
#
# ######################################################################
#
#                           _BPX_SHAREAS variable
#                           ---------------------
# _BPX_SHAREAS=YES improves performance within the shell by allowing
# processes to run in the same address space as the shell.
# ======================================================================
export _BPX_SHAREAS=YES

# ######################################################################
#       bash environment variables
# ======================================================================
 export _ENCODE_FILE_NEW=IBM-1047
 export _ENCODE_FILE_EXISTING=IBM-1047
 export _CEE_RUNOPTS="FILETAG(AUTOCVT,AUTOTAG) POSIX(ON)"
 export _BPXK_AUTOCVT=ON
 export _TAG_REDIR_ERR=txt
 export _TAG_REDIR_IN=txt
 export _TAG_REDIR_OUT=txt

# ######################################################################
#       git  environment variables
# ======================================================================
 export GIT_SHELL=$TOOLS_HOME/bin/bash
 export GIT_EXEC_PATH=$TOOLS_HOME/libexec/git-core
 export GIT_TEMPLATE_DIR=$TOOLS_HOME/share/git-core/templates

# ######################################################################
#       perl environment variables
# ======================================================================
PERL5LIB=$TOOLS_HOME/lib/perl5
PERL5LIB=$PERL5LIB:$TOOLS_HOME/share/perl/5.24.1
export PERL5LIB

# ######################################################################
#       vim environment variables
# ======================================================================
export VIM=$TOOLS_HOME/share/vim/

