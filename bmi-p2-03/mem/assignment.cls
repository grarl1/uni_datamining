% % Math Notes class
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{assignment}[2016/02/10 Assignment class]

% % Class options

% % Unknown options are passed to article
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}

% % Set default options and process
\ExecuteOptions{a4paper}
\ExecuteOptions{10pt}
\ExecuteOptions{english}
\ProcessOptions\relax

% % Load base
\LoadClass{article}

% % Languages
\usepackage[utf8]{inputenc}
\usepackage[T1]{fontenc}
\usepackage{babel}

% % Title
\let\@title\empty
\newcommand{\makepre}
	{
		\maketitle
		\newpage
		\tableofcontents
		\newpage
	}
\newcommand{\subtitle}[1]{\gdef\@subtitle{#1}}
\newcommand{\@subtitle}{}
\newcommand{\university}[1]{\gdef\@university{#1}}
\newcommand{\@university}{}
\renewcommand{\maketitle}{%
	\begin{titlepage}
		\centering
		{\strut\par}
		\vspace{4em}
		{\scshape\LARGE\@university}
		{\par}
		\vspace{4em}
		{\bfseries\Huge\@title}
		{\par}
		\vspace{2em}
		{\huge\@subtitle}
		{\par}
		\vspace{5em}
		{\scshape\Large\@author}
		{\par}
		\vfill
		{\scshape\large\@date}
	\end{titlepage}
}

% % Colors
\usepackage[usenames,dvipsnames]{color}
\definecolor{MyDarkBlue}{rgb}{0.0,0.0,0.3}
\definecolor{MyDarkGreen}{rgb}{0.0,0.4,0.0}

% % Hyperref
\usepackage{hyperref}
\hypersetup{
	colorlinks=true,       % false: boxed links; true: colored links
	linkcolor=MyDarkBlue,  % color of internal links (change box color with linkbordercolor)
	citecolor=MyDarkBlue,  % color of links to bibliography
	filecolor=MyDarkBlue,  % color of file links
	urlcolor=MyDarkBlue    % color of external links
}

% % Listings
\usepackage{listings}

% % Matlab
% For faster processing, load Matlab syntax for listings
\lstloadlanguages{Matlab}%
\lstdefinestyle{matlabStyle}{
		language=Matlab,                        % Use MATLAB
		frame=single,                           % Single frame around code
		basicstyle=\footnotesize\ttfamily,      % Use footnotesize true type font
		keywordstyle=[1]\color{Blue}\bfseries,  % MATLAB functions bold and blue
		keywordstyle=[2]\color{Purple},         % MATLAB function arguments purple
		keywordstyle=[3]\color{Blue}\underbar,  % User functions underlined and blue
		identifierstyle=,                       % Nothing special about identifiers
		% Comments small dark green courier
		commentstyle=\usefont{T1}{pcr}{m}{sl}\color{MyDarkGreen}\small,
		stringstyle=\color{Purple},             % Strings are purple
		showstringspaces=false,                 % Don't put marks in string spaces
		tabsize=4,                              % 5 spaces per tab
		%
		%%% Put standard MATLAB functions not included in the default
		%%% language here
		morekeywords={xlim,ylim,var,alpha,factorial,poissrnd,normpdf,normcdf},
		%
		%%% Put MATLAB function parameters here
		morekeywords=[2]{on, off, interp},
		%
		%%% Put user defined functions here
		morekeywords=[3]{FindESS, homework_example},
		%
		morecomment=[l][\color{Blue}]{...},     % Line continuation (...) like blue comment
		numbers=left,                           % Line numbers on left
		firstnumber=1,                          % Line numbers start with line 1
		numberstyle=\tiny\color{Blue},          % Line numbers are blue
		stepnumber=1       
 	}

% % Header
\usepackage{fancyhdr}
\pagestyle{fancy}
\fancyhead[L]{\@title}
\fancyhead[R]{\leftmark}
\renewcommand{\sectionmark}[1]{\markboth{#1}{}} % Current name of section without converting it to uppercase.
\renewcommand{\tableofcontents}{\section*{\contentsname\@mkboth{\contentsname}{}}\@starttoc{toc}} % Contents name not in uppercase

% % Images and figures
\usepackage{graphicx}
\usepackage{subcaption}
\newcommand{\fimg}[4]{
	\begin{figure}[h]
		\centering
		\includegraphics[#2]{#1}
		\caption{#3}
		\label{fig:#4}
	\end{figure}
}
	
% Tikz
\usepackage{tikz}
\usetikzlibrary{babel}

% More packages

% % End of class
\endinput
