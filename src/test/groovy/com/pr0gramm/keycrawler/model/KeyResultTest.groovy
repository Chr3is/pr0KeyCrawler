package com.pr0gramm.keycrawler.model


import com.pr0gramm.keycrawler.api.Post
import reactor.util.function.Tuples
import spock.lang.Specification
import spock.lang.Unroll

class KeyResultTest extends Specification {

    String keys = """
    Valve CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Valve CD Keys in this format: 1AB2C-D3FGH-456I7
    Age of Empires III: Complete Collection CD Keys in this format: 1ABCD-D3FGH-45I67
    AirBuccaneers CD Keys in this format: 1ABCD-D3FGH-45I67
    Aliens vs. Predator (2010 Release) CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Alpha Prime CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Alter Ego CD Keys in this format: 1AB2C-D3FGH-456I7
    The Amazing Spider-Man CD Keys in this format: ABCD1-E2FGH-3I4J5
    Anomaly: Warzone Earth in this format: 12345-ABCDE-6789F
    Arma 2 titles in this format: ABCD-E2FGH-3I4J5-AB3DE-FGH1J
    Arma 2: Combined Operations CD Keys in this format:  1AB2C-D3FGH-456I7
    Arma 3 Alpha (Supporter Edition) in this format: 12345-ABCDE-6789F
    Audiosurf CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Batman: Arkham City CD Keys in this format: 1AB2C-D3FGH-456I7
    Batman: Arkham Origins CD Keys in this format: 1AB2C-D3FGH-456I7
    The Binding Of Isaac CD Keys in this format: 1AB2C-D3FGH-456I7
    Bioshock CD Keys in this format: 1AB2C-D3FGH-456I7
    Bioshock 2 CD Keys in this format: 1AB2C-D3FGH-456I7
    Bioshock Infinite CD Keys in this format: 1A2BC-DEF34-G5HIJ
    Borderlands CD Keys in this format: 1AB2C-D3FGH-456I7
    Borderlands 2 CD Keys in this format:  1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Botanicula CD keys in this format: ABC1E-FGHIJ-KL123
    BRINK CD Keys in this format:  1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Call of Duty: Modern Warfare 2 CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Call of Duty: Modern Warfare 3 CD Keys in this format: 1AB2C-D3FGH-456I7
    Call of Duty: Black Ops CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Call of Duty: Black Ops 2 CD Keys in this format: 1AB2C-D3FGH-456I7
    Call of Duty: Ghosts CD Keys in this format: 1AB2C-D3FGH-456I7
    Call of Duty 4: Modern Warfare in this format: 1AB2C-D3FGH-456I7
    Chivalry Medieval Warfare CD Keys in this format: 1AB2C-D3FGH-456I7
    Cities in Motion CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Civilization IV: Complete Edition CD Keys in this format: 1AB2C-D3FGH-456I7
    Civilization V CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Civilization V - Gods and Kings CD Keys in this format: 1AB2C-D3FGH-456I7
    Commander: Conquest of the Americas CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Company of Heroes: Opposing Fronts CD Keys in this format: 1AB2-CD3F-GH45-6I7J-K8LM
    Cortex Command CD Keys in this format: 1AB2C-D3FGH-456I7
    Cricket Revolution CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Crusader Kings II CD Keys in this format: 1AB2C-D3FGH-456I7
    The Cursed Crusade CD Keys in this format: 1AB2C-D3FGH-456I7
    The Darkness II CD Keys in this format: 1AB2C-D3FGH-456I7
    Dark Messiah CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Dark Messiah Multiplayer CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Darksiders CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Dark Souls keys in this format:  1AB2C-D3FGH-456I7-JK8LM-NOP9Q
    Dark Souls II keys in this format:  1AB2C-D3FGH-456I7
    Dawn of War Keys in this format: 5BCD-1EFG-HIJK-2LMN
    Masked keys: 1AB2C-D3?GH-456I7-JK8LM-NOP?Q
    """

    def 'all keys are extracted'() {
        given:
        KeyResult keyResult = new KeyResult(Tuples.of(new Post(), keys))

        expect:
        keyResult.keys.size() == 48
    }

    @Unroll
    def 'key=#key is extracted from text=#text'(String text, String key) {
        given:
        KeyResult keyResult = new KeyResult(Tuples.of(new Post(), text))

        expect:
        keyResult.keys.size() == 1
        keyResult.keys[0] == key

        where:
        text                                                                                                    || key
        'Valve CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                               || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Valve CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                                           || '1AB2C-D3FGH-456I7'
        'Age of Empires III: Complete Collection CD Keys in this format: 1ABCD-D3FGH-45I67 aaaaaaaaaaa'         || '1ABCD-D3FGH-45I67'
        'AirBuccaneers CD Keys in this format: 1ABCD-D3FGH-45I67 aaaaaaaaaaa'                                   || '1ABCD-D3FGH-45I67'
        'Aliens vs. Predator (2010 Release) CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'  || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Alpha Prime CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                         || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Alter Ego CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                                       || '1AB2C-D3FGH-456I7'
        'The Amazing Spider-Man CD Keys in this format: ABCD1-E2FGH-3I4J5 aaaaaaaaaaa'                          || 'ABCD1-E2FGH-3I4J5'
        'Anomaly: Warzone Earth in this format: 12345-ABCDE-6789F aaaaaaaaaaa'                                  || '12345-ABCDE-6789F'
        'Arma 2 titles in this format: ABCD-E2FGH-3I4J5-AB3DE-FGH1J aaaaaaaaaaa'                                || 'ABCD-E2FGH-3I4J5-AB3DE-FGH1J'
        'Arma 2: Combined Operations CD Keys in this format:  1AB2C-D3FGH-456I7 aaaaaaaaaaa'                    || '1AB2C-D3FGH-456I7'
        'Arma 3 Alpha (Supporter Edition) in this format: 12345-ABCDE-6789F aaaaaaaaaaa'                        || '12345-ABCDE-6789F'
        'Audiosurf CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                           || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Batman: Arkham City CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                             || '1AB2C-D3FGH-456I7'
        'Batman: Arkham Origins CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                          || '1AB2C-D3FGH-456I7'
        'The Binding Of Isaac CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                            || '1AB2C-D3FGH-456I7'
        'Bioshock CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                                        || '1AB2C-D3FGH-456I7'
        'Bioshock 2 CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                                      || '1AB2C-D3FGH-456I7'
        'Bioshock Infinite CD Keys in this format: 1A2BC-DEF34-G5HIJ aaaaaaaaaaa'                               || '1A2BC-DEF34-G5HIJ'
        'Borderlands CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                                     || '1AB2C-D3FGH-456I7'
        'Borderlands 2 CD Keys in this format:  1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                      || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Botanicula CD keys in this format: ABC1E-FGHIJ-KL123 aaaaaaaaaaa'                                      || 'ABC1E-FGHIJ-KL123'
        'BRINK CD Keys in this format:  1AB2C-D3FGH-456I7-JK8LM-NOP9Q'                                          || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Call of Duty: Modern Warfare 2 CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'      || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Call of Duty: Modern Warfare 3 CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                  || '1AB2C-D3FGH-456I7'
        'Call of Duty: Black Ops CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'             || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Call of Duty: Black Ops 2 CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                       || '1AB2C-D3FGH-456I7'
        'Call of Duty: Ghosts CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                            || '1AB2C-D3FGH-456I7'
        'Call of Duty 4: Modern Warfare in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                          || '1AB2C-D3FGH-456I7'
        'Chivalry Medieval Warfare CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                       || '1AB2C-D3FGH-456I7'
        'Cities in Motion CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                    || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Civilization IV: Complete Edition CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'               || '1AB2C-D3FGH-456I7'
        'Civilization V CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                      || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Civilization V - Gods and Kings CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                 || '1AB2C-D3FGH-456I7'
        'Commander: Conquest of the Americas CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa' || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Company of Heroes: Opposing Fronts CD Keys in this format: 1AB2-CD3F-GH45-6I7J-K8LM aaaaaaaaaaa'       || '1AB2-CD3F-GH45-6I7J-K8LM'
        'Cortex Command CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                                  || '1AB2C-D3FGH-456I7'
        'Cricket Revolution CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                  || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Crusader Kings II CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                               || '1AB2C-D3FGH-456I7'
        'The Cursed Crusade CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                              || '1AB2C-D3FGH-456I7'
        'The Darkness II CD Keys in this format: 1AB2C-D3FGH-456I7 aaaaaaaaaaa'                                 || '1AB2C-D3FGH-456I7'
        'Dark Messiah CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                        || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Dark Messiah Multiplayer CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'            || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Darksiders CD Keys in this format: 1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                          || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Dark Souls keys in this format:  1AB2C-D3FGH-456I7-JK8LM-NOP9Q aaaaaaaaaaa'                            || '1AB2C-D3FGH-456I7-JK8LM-NOP9Q'
        'Dark Souls II keys in this format:  1AB2C-D3FGH-456I7 aaaaaaaaaaa'                                     || '1AB2C-D3FGH-456I7'
        'Dawn of War Keys in this format: 5BCD-1EFG-HIJK-2LMN aaaaaaaaaaa'                                      || '5BCD-1EFG-HIJK-2LMN'
        'Masked keys: 1AB2C-D3?GH-456I7-JK8LM-NOP?Q aaaaaaaaaaa'                                                || '1AB2C-D3?GH-456I7-JK8LM-NOP?Q'
        'GH523-KBF3247-FKJF234-DG63467 aaaaaaaaaaa'                                                             || 'GH523-KBF3247-FKJF234-DG63467'
    }

    @Unroll
    def 'nothing is extracted from text=#text'(String text) {
        given:
        KeyResult keyResult = new KeyResult(Tuples.of(new Post(), text))

        expect:
        keyResult.keys.empty

        where:
        text << ['chwanzldnge?', '[WPA2][ESS][WPS', '26203_47170', 'HEIDELBERG24', 'iber3sxinDeutschland', '2uckerriibensir', 'Robert-Koch-Institut', '11111-11111-11111',
                 '1a111111111111111111111111111111111-abcdef', '000-year-old']
    }

}
