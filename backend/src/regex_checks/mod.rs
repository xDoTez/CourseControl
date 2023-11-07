use regex::{Regex, Error};

pub fn perform_regex_check(regex_string: &str, text: &str) -> Result<bool, Error>
{
    let regex = Regex::new(regex_string)?;

    Ok(regex.is_match(text))
}