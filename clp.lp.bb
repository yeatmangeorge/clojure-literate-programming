;;h1 Clojure Literate Programming
;;h2 Literwhat Programming?
;;p Literate programming is the process of writing code as a human-readable document first. Source code is defined in this document, and a program can be run on the document to produce both the source code and a readable document. This document could output in whatever format you like, be it Markdown, PDF or, HTML. The obvious benefit is when writing a blog about code (not unlike this one). Interweaving the source code and prose in the same file reduces friction, and allows you to verify all the code by running it.
;;p Today, we'll implement a very basic form of literate programming in Babashka. The document you are reading is actually bootstrapped from the code we're going to be writing!
;;h2 Design
;;h3 Prose first vs Code First
;;p First lets zoom out and think how we're going to separate code and prose.
;;p At the time of initially writing this, I've decided to simply write all code in a Babashka file. When we want to write prose, we simply comment and then signal which HTML tag we want. For example, in the source code, the paragraph you are currently reading simply starts with ";;p". This could be deemed a code first approach. We don't have to modify the way we write code much, and our IDEs will simply work as usual. However, it might be more comfortable to write prose in something like a Markdown file. Then all the code would have to go in code blocks.
;;p For the purpose of this article, we are just going to say that code must appear in the order that it would in a normal source file. This is restricting, and there are ways around it that I will talk about later.
;;h3 Source File Discoverability
;;p Initially, I'm going to write the program to simply take in any Babashka file, and output HTML from this. The Babashka file can just be used as is.
;;p One thing I have been thinking about, is automatic file discovery. Do accommodate this, I've decided to name this file "clp.lp.bb". Eventually we can use the ".lp.bb", or ".lp.clj" file suffixes to mean that the Clojure code is literate.
;;h2 MVP 
;;h3 HTML Gen
;;p First lets start by getting the basics working. Let's start by writing some functions to generate basic HTML so we have something to use when we start to generate the output. We could use Hiccup for this, but lets keep the project dependency free for now.
(ns clp.lp)

(defn def-tag-gen
  "We use this to generate tag generators. This is for writing output, not deciding what to generate."
  [tag]
  (fn [content] (str "<" tag ">" content "</" tag ">")))

(def h1 (def-tag-gen "h1"))
(def h2 (def-tag-gen "h2"))
(def h3 (def-tag-gen "h3"))
(def p (def-tag-gen "p"))

;;p Using my REPL, I can test that (p "Hello, World!") produces "<p>Hello, World!</p>".
;;p Cool! Now let's generate an HTML file of all the literate comments so far.
;;h3 Read File and Output HTML Comments
;;p First we shall slurp this file and go through line by line. When we detect that it starts with a comment tag, such as ";;p", let's remove the tag, wrap the rest of the line in an HTML tag, and spit it to the output HTML file.
;;p Let's write a convenience function to detect convert each line into a tagged string:
(require '[clojure.string :as str])

(defn lp-line->HTML [line]
  (let [tag-case (fn [prefix tag-gen]
                   (when (str/starts-with? line prefix)
                     (tag-gen (subs line (count prefix)))))]
    (or (tag-case ";;p " p)
        (tag-case ";;h1 " h1)
        (tag-case ";;h2 " h2)
        (tag-case ";;h3 " h3)
        line)))

;;p Nice! This this works as follows: (lp-line->HTML ";;p Abc, 123") => "<p>Abc, 123</p>"
;;p Now let's try this on the article you are currently reading. Clojure has a cool ability to detect the current file using "*file*".
(require '[babashka.fs :as fs])

(def output-dir ".output")
(def output-html-file (str output-dir "/index.html"))

(defn write-output []
  (fs/create-dirs output-dir)
  (spit output-html-file
        (str/join "\n" (map lp-line->HTML (str/split-lines (slurp *file*))))))

;;p I can open up the generated index.html see it works! But, well, you can't. Let's fix that! I'll add image support and then display an image of the generated HTML page. I'll add image support and then display an image of the generated HTML page. I'll add image support and then display an image of the generated HTML page. I'll add image support and then display an image of the generated HTML page.

;;h2 Adding Image Support
;;p Img is not allowed to have a closing tag, and needs elements such as "src" and "description". Therefore we can't reuse our existing lp-line->HTML function. Let's just do the simplest solution. We'll just see if the line is an img, and if it is, place the contents (elements) of the line inside the tag like so

(defn img [elements]
  (str "<img " elements "/>"))

;;p This can obviously be improved, but for now, this just lets us write "src" and "desc" in a literate programing comment the same way we would if handwriting html. So for example, this is how you would insert an image " ;;img src="http://cat-photos/cat_001.jpg" description="It's a cat" "
;;p One last step! We need to now modify our lp-line->HTML function to allow us to insert images:

(defn lp-line->HTML [line]
  (let [tag-case (fn [prefix tag-gen]
                   (when (str/starts-with? line prefix)
                     (tag-gen (subs line (count prefix)))))]
    (or (tag-case ";;p " p)
        (tag-case ";;h1 " h1)
        (tag-case ";;h2 " h2)
        (tag-case ";;h3 " h3)
        (tag-case ";;img " img)
        line)))

;;p And here's the HTML output, as promised!
;;img src="/Users/georgeyeatman/clojure-literate-programming/1.png" description="A screenshot, showing the generated HTML page we have created so far"
;;h2 Clojure Code
;;p This is fantasitic. Wonderful. Dare I say maginificent? However, there is a problem we haven't fixed yet that I hid with strategic cropping of the screenshot: 
;;img src="/Users/georgeyeatman/clojure-literate-programming/2.png" description="A screenshot, showing that the Clojure code is just dumped into the HTML and doesn't display properly."
;;p As you can see, the Clojure code that is outside of our literate programming comments doesn't read well. This is because, it's currently just input into the HTML file without any surrounding tags. What we need, is for our lp-line->HTML function to detect if it's a line of regular clojure, and if so, put it in a relevant tag. The best way to do this, would be to make the function modal, and wrap each block of Clojure in one div. However, for simplicity, let's just wrap each line of Clojure in it's own "cljLine" div:

(defn clj-line [line]
  (cond
    (str/blank? line) ""
    :else (str "<div class=\"cljLine\">" line "</div>")))

;;p Now, replace the default behavour from lp-line->HTML with this function:

(defn lp-line->HTML [line]
  (let [tag-case (fn [prefix tag-gen]
                   (when (str/starts-with? line prefix)
                     (tag-gen (subs line (count prefix)))))]
    (or (tag-case ";;p " p)
        (tag-case ";;h1 " h1)
        (tag-case ";;h2 " h2)
        (tag-case ";;h3 " h3)
        (tag-case ";;img " img)
        (clj-line line))))

;;p Now the output already looks much better!
;;img src="/Users/georgeyeatman/clojure-literate-programming/3.png" description="The Clojure code now displays much better, however, indentation is incorrect."
;;h3 Fixing Indentation
;;p However, one issue is that the indentation is wrong. If we inspect the generated HTML, we will notice that the inentation is preserved in the HTML, but it is not displayed in the rendered page.
;;img src="/Users/georgeyeatman/clojure-literate-programming/4.png" description="Image showing the preserved whitespace in the raw HTML file"
;;p HTML doesn't preserve whitespace by default, so we'll have to fix this by adding a little bit of CSS to the end of our file 

(spit output-html-file "\n<style> .cljLine { white-space: pre-wrap; } </style>\n" :append true)

;;img src="/Users/georgeyeatman/clojure-literate-programming/5.png" description="Showing the improved indentation"

(write-output)
