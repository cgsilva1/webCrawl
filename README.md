# Web Crawler for USA Today

## Overview
This project implements a web crawler using **Crawler4j** to extract and analyze data from **USA Today**. The crawler efficiently fetches pages, processes metadata, and stores information about discovered URLs, downloaded files, and visited pages.

## Features
- **Multi-threaded crawling** with configurable depth and politeness delay.
- **Content extraction** for HTML, PDFs, images, and text files.
- **Logging and reporting**: Generates CSV files (`fetch_USAToday.csv`, `visit_USAToday.csv`, `urls_USAToday.csv`) summarizing crawler actions.

## Configuration
- **Max Pages to Fetch**: 20,000
- **Crawl Depth**: 16
- **Politeness Delay**: 50ms
- **Number of Crawlers**: 50

## Technologies Used
- **Java 21.0.2**
- **Apache Maven 3.9.9**
- **Crawler4j (v4.4.0)**
- **Log4j (v1.2.17)**
