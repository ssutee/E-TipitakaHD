{
    "package": "com.watnapp.etipitaka.model",
    "prefix": "Database",
    "database": "data.db",
    "tables": ["history", "favorite", "history_item"],
    "history": {
        "columns": [
            {"name": "keywords", "type": "text"},
            {"name": "language", "type": "integer"},
            {"name": "section1", "type": "boolean"},
            {"name": "section2", "type": "boolean"},
            {"name": "section3", "type": "boolean"},
            {"name": "result1", "type": "integer"},
            {"name": "result2", "type": "integer"},
            {"name": "result3", "type": "integer"},
            {"name": "content", "type": "text"}
        ]
    },
    "favorite": {
        "columns": [
            {"name": "note", "type": "text"},
            {"name": "language", "type": "integer"},
            {"name": "volume", "type": "integer"},
            {"name": "page", "type": "integer"},
            {"name": "item", "type": "integer"}
        ]
    },
    "history_item": {
        "columns": [
            {"name": "history_id", "type": "integer"},
            {"name": "volume", "type": "integer"},
            {"name": "page", "type": "integer"},
            {"name": "status", "type": "integer"}
        ]
    }
}