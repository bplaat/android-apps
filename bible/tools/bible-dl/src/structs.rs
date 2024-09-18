/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

use serde::Deserialize;
use serde_json::Value;

// Metadata
#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Metadata {
    pub abbreviation: String,
    pub abbreviation_local: String,
    pub name_local: String,
    pub copyright: String,
    pub language: Language,
    pub updated_at: String,
    pub testaments: Vec<Testament>,
}

#[derive(Deserialize)]
pub struct Language {
    pub id: String,
}

#[derive(Deserialize)]
pub struct Testament {
    pub abbreviation: String,
    pub books: Vec<Book>,
}

#[derive(Deserialize)]
pub struct Book {
    pub id: String,
    pub name: String,
    pub chapters: Vec<Chapter>,
}

#[derive(Deserialize)]
pub struct Chapter {
    pub number: String,
}

// Chapter contents
#[derive(Deserialize)]
pub struct ChapterContents {
    #[serde(rename = "content")]
    pub blocks: Vec<Block>,
}

#[derive(Deserialize)]
pub struct Block {
    pub r#type: String,
    pub content: Option<Vec<BlockContent>>,
}

#[derive(Deserialize)]
pub struct BlockContent {
    pub r#type: String,
    pub content: Option<Value>,
}
