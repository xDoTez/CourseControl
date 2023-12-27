use fancy_regex::{Error, Regex};

pub fn perform_regex_check(regex_string: &str, text: &str) -> Result<bool, Error> {
    let regex = Regex::new(regex_string)?;

    match regex.is_match(text) {
        Ok(result) => Ok(result),
        Err(error) => Err(error),
    }
}
