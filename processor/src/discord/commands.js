const { SlashCommandBuilder } = require('discord.js');

const EVENT_DESC = '이벤트 종류 (HOT: 급상승, START: 방송 시작, END: 방송 종료, TOPIC: 키워드 감지)';
const TARGET_DESC = '방송자 채널 ID (비워두면 전체 방송자)';
const KEYWORD_DESC = 'TOPIC 이벤트 전용 키워드 (예: 리그 오브 레전드)';

const commands = [

  // /help
  new SlashCommandBuilder()
    .setName('help')
    .setDescription('사용 가능한 명령어를 안내합니다.'),

  // /subscribe
  new SlashCommandBuilder()
    .setName('subscribe')
    .setDescription('이벤트 구독 - 예: /subscribe TOPIC 리그 오브 레전드')
    .addStringOption(option =>
      option.setName('event')
        .setDescription(EVENT_DESC)
        .setRequired(true)
    )
    .addStringOption(option =>
      option.setName('target')
        .setDescription(TARGET_DESC)
        .setRequired(false)
    )
    .addStringOption(option =>
      option.setName('keyword')
        .setDescription(KEYWORD_DESC)
        .setRequired(false)
    ),

  // /unsubscribe
  new SlashCommandBuilder()
    .setName('unsubscribe')
    .setDescription('구독 해제 - 예: /unsubscribe TOPIC 리그 오브 레전드')
    .addStringOption(option =>
      option.setName('event')
        .setDescription(EVENT_DESC)
        .setRequired(false)
    )
    .addStringOption(option =>
      option.setName('target')
        .setDescription(TARGET_DESC)
        .setRequired(false)
    )
    .addStringOption(option =>
      option.setName('keyword')
        .setDescription(KEYWORD_DESC)
        .setRequired(false)
    ),

  // /subscriptions
  new SlashCommandBuilder()
    .setName('subscriptions')
    .setDescription('구독 목록 조회 - 예: /subscriptions TOPIC 리그 오브 레전드')
    .addStringOption(option =>
      option.setName('event')
        .setDescription(EVENT_DESC)
        .setRequired(false)
    )
    .addStringOption(option =>
      option.setName('target')
        .setDescription(TARGET_DESC)
        .setRequired(false)
    )
    .addStringOption(option =>
      option.setName('keyword')
        .setDescription(KEYWORD_DESC)
        .setRequired(false)
    ),

];

module.exports = commands;
