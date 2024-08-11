use serde::{Deserialize, Serialize};
use serde_json::Value;

// Metadata
#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Metadata {
    pub id: String,
    pub dbl_id: String,
    pub abbreviation: String,
    pub abbreviation_local: String,
    pub name: String,
    pub name_local: String,
    pub description: String,
    pub description_local: String,
    pub copyright: String,
    pub language: Language,
    pub countries: Vec<Country>,
    pub updated_at: String,
    pub testaments: Vec<Testament>,
    pub position: i64,
    pub licensed_for_mobile_app: bool,
    pub licensed_for_web: bool,
    pub has_audio: bool,
    pub audio_access_level_mobile: String,
    pub audio_access_level_web: String,
    pub custom_styling: CustomStyling,
    pub image_url: String,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Language {
    pub id: String,
    pub name: String,
    pub name_local: String,
    pub script: String,
    pub script_direction: String,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Country {
    pub id: String,
    pub name: String,
    pub name_local: String,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Testament {
    pub abbreviation: String,
    pub books: Vec<Book>,
    pub order: i64,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Book {
    pub id: String,
    pub bible_id: String,
    pub abbreviation: String,
    pub name: String,
    pub name_long: String,
    pub chapters: Vec<Chapter>,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Chapter {
    pub id: String,
    pub bible_id: String,
    pub book_id: String,
    pub number: String,
    pub position: i64,
    pub has_audio: bool,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CustomStyling {
    pub nd: Value,
    pub wj: Value,
}

// Chapter contents
#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ChapterContents {
    pub bible_id: String,
    pub book_id: String,
    pub id: String,
    #[serde(rename = "content")]
    pub blocks: Vec<Block>,
    pub number: String,
    pub copyright: Option<String>,
    pub verse_count: i64,
    pub title: String,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Block {
    pub r#type: String,
    pub style: String,
    pub content: Option<Vec<BlockContent>>,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct BlockContent {
    pub r#type: String,
    pub content: Option<String>,
    pub style: Option<String>,
    pub verse_id: Option<String>,
    #[serde(default)]
    pub verse_org_id: Vec<String>,
    pub has_footnotes: Option<bool>,
    pub id: Option<String>,
    pub subtype: Option<String>,
}
